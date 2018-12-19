package common;

import java.util.Arrays;

public class ByteIterator {
    protected byte[] buffer;
    protected int index = 0;

    public ByteIterator(byte[] buffer) {
        this.buffer = buffer;
    }

    public boolean hasNext() {
        return this.index < this.buffer.length;
    }

    public byte[] next(long chunk2) {
        int chunk = (int) chunk2;
        if (this.index >= this.buffer.length) {
            return new byte[0];
        }
        if (this.index + chunk < this.buffer.length) {
            byte[] chunkb = Arrays.copyOfRange(this.buffer, this.index, this.index + chunk);
            this.index += chunk;
            return chunkb;
        }
        byte[] chunkb = Arrays.copyOfRange(this.buffer, this.index, this.buffer.length);
        this.index = this.buffer.length;
        return chunkb;
    }

    public void reset() {
        this.index = 0;
    }

    public static void test1() {
        byte[] garbage = CommonUtils.randomData(CommonUtils.rand(10485760));
        CommonUtils.print_warn("Garbage is: " + garbage.length);
        String before = CommonUtils.toHex(CommonUtils.MD5(garbage));
        ByteIterator munch = new ByteIterator(garbage);
        byte[] result = new byte[]{};
        int x = 0;
        while (munch.hasNext()) {
            byte[] next = munch.next(0x100000L);
            CommonUtils.print_warn("Chunk " + x + ": " + next.length);
            result = CommonUtils.join(result, next);
            ++x;
        }
        String after = CommonUtils.toHex(CommonUtils.MD5(result));
        CommonUtils.print_info("MD5 (before): " + before);
        CommonUtils.print_info("MD5  (after): " + after);
        if (!before.equals(after)) {
            CommonUtils.print_error("FAILED!");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        for (int x = 0; x < 8192; ++x) {
            ByteIterator.test1();
        }
        CommonUtils.print_good("PASSED!");
    }
}

