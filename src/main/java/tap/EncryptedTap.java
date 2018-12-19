package tap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class EncryptedTap
        extends TapProtocol {
    protected SecretKey key;
    protected IvParameterSpec ivspec;
    protected Cipher in;
    protected Cipher out;
    protected byte[] out_buffer = new byte[65536];
    protected byte[] in_buffer = new byte[65536];
    protected ByteArrayOutputStream out_bytes;
    protected DataOutputStream out_handle;

    public EncryptedTap(String string, byte[] arrby) {
        super(string);
        byte[] arrby2 = new byte[16];
        for (int i = 0; i < arrby2.length; ++i) {
            arrby2[i] = arrby[i % arrby.length];
        }
        try {
            this.key = new SecretKeySpec(arrby2, "AES");
            byte[] arrby3 = "abcdefghijklmnop".getBytes();
            this.ivspec = new IvParameterSpec(arrby3);
            this.in = Cipher.getInstance("AES/CBC/NoPadding");
            this.out = Cipher.getInstance("AES/CBC/NoPadding");
            this.out_bytes = new ByteArrayOutputStream(65536);
            this.out_handle = new DataOutputStream(this.out_bytes);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected void pad(ByteArrayOutputStream byteArrayOutputStream) {
        for (int i = byteArrayOutputStream.size() % 16; i < 16; ++i) {
            byteArrayOutputStream.write(65);
        }
    }

    @Override
    public byte[] protocol(int n, byte[] arrby) {
        byte[] arrby2 = super.protocol(n, arrby);
        try {
            this.out_bytes.reset();
            this.out_handle.write(arrby2, 0, arrby2.length);
            this.pad(this.out_bytes);
            this.in.init(1, this.key, this.ivspec);
            return this.in.doFinal(this.out_bytes.toByteArray());
        } catch (Exception exception) {
            exception.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public int readFrame(byte[] arrby) {
        int n = super.readFrame(this.out_buffer);
        try {
            this.out_bytes.reset();
            this.out_handle.writeShort(n);
            this.out_handle.write(this.out_buffer, 0, n);
            this.pad(this.out_bytes);
            this.in.init(1, this.key, this.ivspec);
            byte[] arrby2 = this.in.doFinal(this.out_bytes.toByteArray());
            System.arraycopy(arrby2, 0, arrby, 0, arrby2.length);
            return arrby2.length;
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    @Override
    public void writeFrame(byte[] arrby, int n) {
        try {
            this.out.init(2, this.key, this.ivspec);
            byte[] arrby2 = this.out.doFinal(arrby, 0, n);
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrby2));
            int n2 = dataInputStream.readUnsignedShort();
            dataInputStream.readFully(this.in_buffer, 0, n2);
            this.writeFrame(this.fd, this.in_buffer, n2);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

