package c2profile;

import common.CommonUtils;

import java.util.Iterator;
import java.util.LinkedList;

public class SmartBuffer {
    protected LinkedList<byte[]> data = new LinkedList<>();
    protected int prepend_len = 0;

    public int getDataOffset() {
        return this.prepend_len;
    }

    public void append(byte[] temp) {
        this.data.add(temp);
    }

    public void prepend(byte[] temp) {
        this.data.add(0, temp);
        this.prepend_len += temp.length;
    }

    public void clear() {
        this.prepend_len = 0;
        this.data.clear();
    }

    public void strrep(String orig, String repl) {
        LinkedList<byte[]> tmp = new LinkedList<>(this.data);
        this.clear();
        for (byte[] temp : tmp) {
            if (temp.length >= orig.length()) {
                this.append(CommonUtils.strrep(temp, orig, repl));
                continue;
            }
            this.append(temp);
        }
    }

    public Iterator<byte[]> iterator() {
        return this.data.iterator();
    }

    public byte[] getBytes() {
        if (this.data.size() == 1) {
            return this.data.getFirst();
        }
        if (this.data.size() == 0) {
            return new byte[0];
        }
        byte[] result = new byte[this.size()];
        int y = 0;
        for (Object aData : this.data) {
            byte[] temp = (byte[]) aData;
            System.arraycopy(temp, 0, result, y, temp.length);
            y += temp.length;
        }
        return result;
    }

    public int size() {
        if (this.data.size() == 1) {
            return this.data.getFirst().length;
        }
        if (this.data.size() == 0) {
            return 0;
        }
        int sz = this.data.stream().map(aData -> (byte[]) aData).mapToInt(temp -> temp.length).sum();
        return sz;
    }

    public String toString() {
        byte[] r = this.getBytes();
        StringBuilder z = new StringBuilder();
        for (byte aR : r) {
            char next = (char) aR;
            if (Character.isDigit(next) || Character.isLetter(next) || Character.isWhitespace(next) || next == '%' || next == '!' || next == '.') {
                z.append(next);
                continue;
            }
            z.append("[").append(aR).append("]");
        }
        return z.toString();
    }
}

