package beacon.dns;

import common.Packer;

import java.math.BigInteger;

public class RecvConversation {
    protected String id;
    protected String dtype;
    protected long size = -1L;
    protected Packer buffer = new Packer();

    public RecvConversation(String id, String dtype) {
        this.id = id;
        this.dtype = dtype;
    }

    public long next(String data) {
        if (this.size == -1L) {
            BigInteger temp = new BigInteger(data, 16);
            this.size = temp.longValue();
        } else {
            this.buffer.addHex(data);
        }
        return 0L;
    }

    public boolean isComplete() {
        return this.buffer.size() >= this.size;
    }

    public byte[] result() {
        return this.buffer.getBytes();
    }

    public String toString() {
        return "[id: " + this.id + ", type: " + this.dtype + ", recv'd: " + this.buffer.size() + ", total: " + this.size + "]";
    }
}

