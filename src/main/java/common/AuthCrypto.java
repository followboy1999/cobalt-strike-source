package common;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

public class AuthCrypto {
    public Cipher cipher;
    public Key pubkey = null;
    protected String error = null;

    public AuthCrypto() {
        try {
            this.cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            this.load();
        } catch (Exception ex) {
            this.error = "Could not initialize crypto";
            MudgeSanity.logException("AuthCrypto init", ex, false);
        }
    }

    public void load() {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(CommonUtils.readResource("resources/authkey.pub"));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.pubkey = keyFactory.generatePublic(spec);
        } catch (Exception ex) {
            this.error = "Could not deserialize authpub.key";
            MudgeSanity.logException("authpub.key deserialization", ex, false);
        }
    }

    public String error() {
        return this.error;
    }

    public byte[] decrypt(byte[] ciphertext) {
        byte[] plaintext = this._decrypt(ciphertext);
        try {
            if (plaintext.length == 0) {
                return plaintext;
            }
            DataParser parser = new DataParser(plaintext);
            parser.big();
            if (parser.readInt() != -889274181) {
                this.error = "bad header";
                return new byte[0];
            }
            int length = parser.readShort();
            byte[] data = parser.readBytes(length);
            return CommonUtils.gunzip(data);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return new byte[0];
        }
    }

    protected byte[] _decrypt(byte[] ciphertext) {
        byte[] plaintext = new byte[]{};
        try {
            if (this.pubkey == null) {
                return new byte[0];
            }
            Cipher cipher = this.cipher;
            synchronized (cipher) {
                this.cipher.init(2, this.pubkey);
                plaintext = this.cipher.doFinal(ciphertext);
            }
            return plaintext;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return new byte[0];
        }
    }
}

