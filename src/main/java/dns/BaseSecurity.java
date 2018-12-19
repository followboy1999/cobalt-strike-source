package dns;

import common.CommonUtils;
import common.MudgeSanity;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseSecurity {
    public static final short CRYPTO_LICENSED_PRODUCT = 0;
    public static final short CRYPTO_TRIAL_PRODUCT = 1;
    protected IvParameterSpec ivspec;
    protected final Cipher in;
    protected final Cipher out;
    protected final Mac mac;
    protected static Map<String, Session> keymap = new HashMap<>();

    protected SecretKey getKey(String id) {
        Session sess = this.getSession(id);
        if (sess != null) {
            return sess.key;
        }
        return null;
    }

    protected SecretKey getHashKey(String id) {
        Session sess = this.getSession(id);
        if (sess != null) {
            return sess.hash_key;
        }
        return null;
    }

    public boolean isReady(String id) {
        return this.getSession(id) != null;
    }

    protected Session getSession(String id) {
        return (Session) keymap.get(id);
    }

    public void registerKey(String id, byte[] keyb) {
        if (keymap.containsKey(id)) {
            return;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key_hash = digest.digest(keyb);
            byte[] crypt_keyb = Arrays.copyOfRange(key_hash, 0, 16);
            byte[] hmac_keyb = Arrays.copyOfRange(key_hash, 16, 32);
            Session temp = new Session();
            temp.key = new SecretKeySpec(crypt_keyb, "AES");
            temp.hash_key = new SecretKeySpec(hmac_keyb, "HmacSHA256");
            keymap.put(id, temp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BaseSecurity() {
        try {
            byte[] iv = "abcdefghijklmnop".getBytes();
            this.ivspec = new IvParameterSpec(iv);
            this.in = Cipher.getInstance("AES/CBC/NoPadding");
            this.out = Cipher.getInstance("AES/CBC/NoPadding");
            this.mac = Mac.getInstance("HmacSHA256");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void pad(ByteArrayOutputStream out) {
        for (int pad = out.size() % 16; pad < 16; ++pad) {
            out.write(65);
        }
    }

    public void debugFrame(String label, byte[] data) {
        try {
            StringBuilder output = new StringBuilder();
            output.append("== ").append(label).append(" ==\n");
            DataInputStream in_handle = new DataInputStream(new ByteArrayInputStream(data));
            int last = in_handle.readInt();
            output.append("\tReplay Counter: ").append(last).append("\n");
            int len = in_handle.readInt();
            output.append("\tMessage Length: ").append(len).append("\n");
            byte[] result = new byte[len];
            in_handle.readFully(result, 0, len);
            output.append("\tPlain Text:     ").append(CommonUtils.toHexString(result)).append("\n");
            CommonUtils.print_good(output.toString());
        } catch (Exception ex) {
            MudgeSanity.logException("foo", ex, false);
        }
    }

    public byte[] encrypt(String id, byte[] data) {
        try {
            if (!this.isReady(id)) {
                CommonUtils.print_error("encrypt: No session for '" + id + "'");
                return new byte[0];
            }
            ByteArrayOutputStream out_bytes = new ByteArrayOutputStream(data.length + 1024);
            DataOutputStream out_handle = new DataOutputStream(out_bytes);
            SecretKey key = this.getKey(id);
            SecretKey hash_key = this.getHashKey(id);
            out_bytes.reset();
            out_handle.writeInt((int) (System.currentTimeMillis() / 1000L));
            out_handle.writeInt(data.length);
            out_handle.write(data, 0, data.length);
            this.pad(out_bytes);
            byte[] encrypted;
            synchronized (this.in) {
                encrypted = this.do_encrypt(key, out_bytes.toByteArray());
            }
            byte[] hmac;
            synchronized (this.mac) {
                this.mac.init(hash_key);
                hmac = this.mac.doFinal(encrypted);
            }
            ByteArrayOutputStream out_stream = new ByteArrayOutputStream();
            out_stream.write(encrypted);
            out_stream.write(hmac, 0, 16);
            return out_stream.toByteArray();
        } catch (InvalidKeyException ikx) {
            MudgeSanity.logException("encrypt failure for: " + id, ikx, false);
            CommonUtils.print_error_file("resources/crypto.txt");
            MudgeSanity.debugJava();
            SecretKey key = this.getKey(id);
            if (key != null) {
                CommonUtils.print_info("Key's algorithm is: '" + key.getAlgorithm() + "' ivspec is: " + this.ivspec);
            }
        } catch (Exception ex) {
            MudgeSanity.logException("encrypt failure for: " + id, ex, false);
        }
        return new byte[0];
    }

    public byte[] decrypt(String id, byte[] frame) {
        try {
            if (!this.isReady(id)) {
                CommonUtils.print_error("decrypt: No session for '" + id + "'");
                return new byte[0];
            }
            Session sess = this.getSession(id);
            SecretKey key = this.getKey(id);
            SecretKey hash_key = this.getHashKey(id);
            byte[] encrypted = Arrays.copyOfRange(frame, 0, frame.length - 16);
            byte[] hmac = Arrays.copyOfRange(frame, frame.length - 16, frame.length);
            byte[] our_hmac_full;
            synchronized (this.mac) {
                this.mac.init(hash_key);
                our_hmac_full = this.mac.doFinal(encrypted);
            }
            byte[] our_hmac = Arrays.copyOfRange(our_hmac_full, 0, 16);
            if (!MessageDigest.isEqual(hmac, our_hmac)) {
                CommonUtils.print_error("[Session Security] Bad HMAC on " + frame.length + " byte message from Beacon " + id);
                return new byte[0];
            }
            byte[] decrypted;
            synchronized (this.out) {
                decrypted = this.do_decrypt(key, encrypted);
            }
            DataInputStream in_handle = new DataInputStream(new ByteArrayInputStream(decrypted));
            int last = in_handle.readInt();
            if ((long) last <= sess.counter) {
                CommonUtils.print_error("[Session Security] Bad counter (replay attack?) " + last + " <= " + sess.counter + " message from Beacon " + id);
                return new byte[0];
            }
            int len = in_handle.readInt();
            if (len < 0 || len > frame.length) {
                CommonUtils.print_error("[Session Security] Impossible message length: " + len + " from Beacon " + id);
                return new byte[0];
            }
            byte[] result = new byte[len];
            in_handle.readFully(result, 0, len);
            sess.counter = last;
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new byte[0];
        }
    }

    public static void main(String[] args) {
        QuickSecurity qs = new QuickSecurity();
        qs.registerKey("1234", CommonUtils.randomData(16));
        String test = "This is a test string, I want to see what happens.";
        byte[] testb = CommonUtils.toBytes(test);
        byte[] cipher = qs.encrypt("1234", testb);
        byte[] plain = qs.decrypt("1234", cipher);
        CommonUtils.print_info("Cipher [H]:  " + CommonUtils.toHexString(cipher));
        CommonUtils.print_info("Plain  [H]:  " + CommonUtils.toHexString(plain));
        CommonUtils.print_info("Cipher:      " + CommonUtils.bString(cipher).replaceAll("\\P{Print}", "."));
        CommonUtils.print_info("Plain:       " + CommonUtils.bString(plain));
        CommonUtils.print_info("[Cipher]:    " + cipher.length);
        CommonUtils.print_info("[Plain]:     " + plain.length);
        System.out.println("SCHEME" + QuickSecurity.getCryptoScheme());
    }

    protected abstract byte[] do_encrypt(SecretKey var1, byte[] var2) throws Exception;

    protected abstract byte[] do_decrypt(SecretKey var1, byte[] var2) throws Exception;

    private static class Session {
        public SecretKey key = null;
        public SecretKey hash_key = null;
        public long counter = 0L;

        private Session() {
        }
    }

}

