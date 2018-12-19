package common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Stack;

public class DataParser {
    protected DataInputStream content;
    protected byte[] bdata = new byte[8];
    protected ByteBuffer buffer;
    protected byte[] original;
    protected Stack frames = new Stack();

    public DataParser(InputStream i) {
        this(CommonUtils.readAll(i));
    }

    public void jump(long offset) throws IOException {
        this.frames.push(this.content);
        this.content = new DataInputStream(new ByteArrayInputStream(this.original));
        if (offset > 0L) {
            this.consume((int) offset);
        }
    }

    public void complete() throws IOException {
        this.content.close();
        this.content = (DataInputStream) this.frames.pop();
    }

    public DataParser(byte[] data) {
        this.original = data;
        this.buffer = ByteBuffer.wrap(this.bdata);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.content = new DataInputStream(new ByteArrayInputStream(data));
    }

    public void consume(int x) throws IOException {
        this.content.skipBytes(x);
    }

    public int readInt() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 4);
        return this.buffer.getInt(0);
    }

    public long readQWord() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 8);
        return this.buffer.getLong(0);
    }

    public byte readByte() throws IOException {
        return this.content.readByte();
    }

    public char readChar() throws IOException {
        return (char) this.content.readByte();
    }

    public char readChar(DataInputStream c) throws IOException {
        return (char) c.readByte();
    }

    public byte[] readBytes(int total) throws IOException {
        byte[] temp = new byte[total];
        this.content.read(temp);
        return temp;
    }

    public int readShort() throws IOException {
        this.content.read(this.bdata, 0, 2);
        return this.buffer.getShort(0) & 65535;
    }

    public boolean more() throws IOException {
        return this.content.available() > 0;
    }

    public String readCountedString() throws IOException {
        int length = this.readInt();
        StringBuilder res = new StringBuilder();
        for (int x = 0; x < length; ++x) {
            res.append(this.readChar());
        }
        return res.toString();
    }

    public String readString() throws IOException {
        char next;
        StringBuilder r = new StringBuilder();
        while ((next = this.readChar()) > '\u0000') {
            r.append(next);
        }
        return r.toString();
    }

    public String readString(int len) throws IOException {
        StringBuilder r = new StringBuilder();
        for (int x = 0; x < len; ++x) {
            char next = this.readChar();
            if (next <= '\u0000') continue;
            r.append(next);
        }
        return r.toString();
    }

    public DataInputStream getData() {
        return this.content;
    }

    public void little() {
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void big() {
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
}

