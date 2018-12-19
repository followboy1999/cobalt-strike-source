package tap;

public class ObfuscatedTap
        extends TapProtocol {
    protected byte[] xor_key = new byte[1024];

    public ObfuscatedTap(String string, byte[] arrby) {
        super(string);
        for (int i = 0; i < this.xor_key.length; ++i) {
            this.xor_key[i] = arrby[i % arrby.length];
        }
    }

    @Override
    public int readFrame(byte[] arrby) {
        int n = super.readFrame(arrby);
        for (int i = 0; i < n; ++i) {
            arrby[i] = (byte) (arrby[i] ^ this.xor_key[i % 1024]);
        }
        return n;
    }

    @Override
    public void writeFrame(byte[] arrby, int n) {
        for (int i = 0; i < n; ++i) {
            arrby[i] = (byte) (arrby[i] ^ this.xor_key[i % 1024]);
        }
        this.writeFrame(this.fd, arrby, n);
    }

    @Override
    public byte[] protocol(int n, byte[] arrby) {
        byte[] arrby2 = super.protocol(n, arrby);
        for (int i = 0; i < arrby2.length; ++i) {
            arrby2[i] = (byte) (arrby2[i] ^ this.xor_key[i % 1024]);
        }
        return arrby2;
    }
}

