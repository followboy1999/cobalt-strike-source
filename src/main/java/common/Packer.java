package common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Packer {
    protected ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    protected DataOutputStream data = new DataOutputStream(this.out);
    protected byte[] bdata = new byte[8];
    protected ByteBuffer buffer = ByteBuffer.wrap(this.bdata);

    public void little() {
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void big() {
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }

    public void addInteger(int x) {
        this.addInt(x);
    }

    public void addInt(int x) {
        this.buffer.putInt(0, x);
        this.write(this.bdata, 0, 4);
    }

    public void append(byte[] data) {
        this.write(data, 0, data.length);
    }

    public void addIntWithMask(int x, int mask) {
        this.buffer.putInt(0, x);
        ByteOrder current = this.buffer.order();
        this.big();
        int temp = this.buffer.getInt(0);
        this.buffer.putInt(0, temp ^ mask);
        this.write(this.bdata, 0, 4);
        this.buffer.order(current);
    }

    public void addUnicodeString(String text, int max) {
        try {
            this.addShort(text.length());
            this.addShort(max);
            for (int x = 0; x < text.length(); ++x) {
                this.data.writeChar(text.charAt(x));
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("addUnicodeString: " + text, ioex, false);
        }
    }

    public void addByte(int b) {
        try {
            this.data.write((byte) b);
        } catch (IOException ioex) {
            MudgeSanity.logException("addByte: " + b, ioex, false);
        }
    }

    public void addHex(String dataz) {
        try {
            char[] tempchars = dataz.toCharArray();
            StringBuilder number = new StringBuilder("FF");
            for (int y = 0; y < tempchars.length; y += 2) {
                number.setCharAt(0, tempchars[y]);
                number.setCharAt(1, tempchars[y + 1]);
                this.data.writeByte(Integer.parseInt(number.toString(), 16));
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("addHex: " + dataz, ioex, false);
        }
    }

    protected void write(byte[] src, int start, int len) {
        try {
            this.data.write(src, start, len);
        } catch (IOException ioex) {
            MudgeSanity.logException("write", ioex, false);
        }
    }

    public void addShort(int x) {
        this.buffer.putShort(0, (short) x);
        this.write(this.bdata, 0, 2);
    }

    public void addString(String src) {
        this.addString(src, src.length());
    }

    public void addString(String src, int total) {
        this.addString(CommonUtils.toBytes(src), total);
    }

    public void pad(char data, int total) {
        byte[] padding = new byte[total];
        for (int x = 0; x < padding.length; ++x) {
            padding[x] = (byte) data;
        }
        this.write(padding, 0, padding.length);
    }

    public void addString(byte[] src, int total) {
        this.write(src, 0, src.length);
        if (src.length < total) {
            byte[] empty = new byte[total - src.length];
            for (int x = 0; x < empty.length; ++x) {
                empty[x] = 0;
            }
            this.write(empty, 0, empty.length);
        }
    }

    public void addStringUTF8(String src, int total) {
        try {
            this.addString(src.getBytes(StandardCharsets.UTF_8), total);
        } catch (Exception ex) {
            MudgeSanity.logException("addStringUTF8", ex, false);
        }
    }

    public void addWideString(String src) {
        try {
            this.append(src.getBytes(StandardCharsets.UTF_16LE));
        } catch (Exception ex) {
            MudgeSanity.logException("addWideString", ex, false);
        }
    }

    public void addWideString(String src, int total) {
        try {
            this.addString(src.getBytes(StandardCharsets.UTF_16LE), total);
        } catch (Exception ex) {
            MudgeSanity.logException("addWideString", ex, false);
        }
    }

    public byte[] getBytes() {
        byte[] result = this.out.toByteArray();
        try {
            this.data.close();
        } catch (IOException ioex) {
            MudgeSanity.logException("getBytes", ioex, false);
        }
        return result;
    }

    public long size() {
        return this.out.size();
    }
}

