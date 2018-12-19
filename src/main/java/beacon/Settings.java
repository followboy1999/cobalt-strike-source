package beacon;

import common.AssertUtils;
import common.CommonUtils;
import common.Packer;

public class Settings {
    public static final int PATCH_SIZE = 4096;
    public static final int MAX_SETTINGS = 64;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_SHORT = 1;
    public static final int TYPE_INT = 2;
    public static final int TYPE_PTR = 3;
    protected Packer patch = new Packer();

    public void addShort(int index, int value) {
        AssertUtils.TestRange(index, 0, 64);
        this.patch.addShort(index);
        this.patch.addShort(1);
        this.patch.addShort(2);
        this.patch.addShort(value);
    }

    public void addInt(int index, int value) {
        AssertUtils.TestRange(index, 0, 64);
        this.patch.addShort(index);
        this.patch.addShort(2);
        this.patch.addShort(4);
        this.patch.addInt(value);
    }

    public void addData(int index, byte[] data, int length) {
        AssertUtils.TestRange(index, 0, 64);
        this.patch.addShort(index);
        this.patch.addShort(3);
        this.patch.addShort(length);
        this.patch.addString(data, length);
    }

    public void addString(int index, String data, int length) {
        this.addData(index, CommonUtils.toBytes(data), length);
    }

    public byte[] toPatch() {
        return this.toPatch(4096);
    }

    public byte[] toPatch(int max) {
        this.patch.addShort(0);
        byte[] result = this.patch.getBytes();
        AssertUtils.Test(result.length < max, "Patch " + result.length + " bytes is too large! Beacon will crash");
        return this.patch.getBytes();
    }
}

