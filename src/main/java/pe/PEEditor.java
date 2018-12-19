package pe;

import common.CommonUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;

public class PEEditor {
    protected PEParser info = null;
    protected byte[] data;
    protected byte[] bdata = new byte[8];
    protected ByteBuffer buffer;
    protected int origch = 0;

    public byte[] getImage() {
        return this.data;
    }

    public void checkAssertions() {
        this.getInfo();
        int flags = -8483;
        if ((this.origch & flags) != 0) {
            CommonUtils.print_error("Beacon DLL has a Characteristic that's unexpected\n\tFlags: " + Integer.toBinaryString(flags) + "\n\tOrigc: " + Integer.toBinaryString(this.origch));
        }
    }

    public PEParser getInfo() {
        if (this.info == null) {
            this.info = PEParser.load(this.data);
            this.origch = this.getInfo().get("Characteristics");
        }
        return this.info;
    }

    public PEEditor(byte[] data) {
        this.data = data;
        this.buffer = ByteBuffer.wrap(this.bdata);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void updateChecksum() {
        long checksum = this.getInfo().checksum();
        this.setChecksum(checksum);
    }

    public void setModuleStomp(String lib) {
        this.setCharacteristic(16384, true);
        this.setString(64, CommonUtils.randomData(64));
        this.setStringZ(64, lib);
    }

    public void stompPE() {
        this.setCharacteristic(1, true);
    }

    public void insertRichHeader(byte[] rich_header) {
        this.removeRichHeader();
        if (rich_header.length == 0) {
            return;
        }
        long e_lfanew = this.getInfo().get("e_lfanew");
        this.setValueAt("e_lfanew", e_lfanew + (long) rich_header.length);
        byte[] dos_header = Arrays.copyOfRange(this.data, 0, 128);
        byte[] pe_header = Arrays.copyOfRange(this.data, (int) e_lfanew, 1024 - rich_header.length);
        byte[] newh = CommonUtils.join(dos_header, rich_header, pe_header);
        System.arraycopy(newh, 0, this.data, 0, 1024);
        this.info = PEParser.load(this.data);
    }

    public void removeRichHeader() {
        if (this.getInfo().getRichHeaderSize() == 0) {
            return;
        }
        long e_lfanew = this.getInfo().get("e_lfanew");
        this.setValueAt("e_lfanew", 128L);
        byte[] dos_header = Arrays.copyOfRange(this.data, 0, 128);
        byte[] pe_header = Arrays.copyOfRange(this.data, (int) e_lfanew, 1024);
        byte[] padding = new byte[1024 - (dos_header.length + pe_header.length)];
        byte[] newh = CommonUtils.join(dos_header, pe_header, padding);
        System.arraycopy(newh, 0, this.data, 0, 1024);
        this.info = PEParser.load(this.data);
    }

    public void setExportName(String value) {
        int offset = CommonUtils.bString(this.data).indexOf(value + '\u0000');
        if (offset > 0) {
            int addr = this.getInfo().getLocation("Export.Name");
            int ptr = this.getInfo().getPointerForLocation(0, offset);
            this.setLong(addr, ptr);
        } else {
            CommonUtils.print_warn("setExportName() failed. " + value + " not found in strings table");
        }
    }

    public void setChecksum(long checksum) {
        this.setLong(this.getInfo().getLocation("CheckSum"), checksum);
    }

    public void setAddressOfEntryPoint(long value) {
        this.setValueAt("AddressOfEntryPoint", value);
    }

    public void setEntryPoint(long value) {
        long ep = this.getInfo().get("AddressOfEntryPoint");
        this.setValueAt("LoaderFlags", ep);
        this.setCharacteristic(4096, true);
        this.setAddressOfEntryPoint(value);
    }

    protected void setString(int offset, byte[] value) {
        for (int x = 0; x < value.length; ++x) {
            this.data[x + offset] = value[x];
        }
    }

    protected void setStringZ(int offset, String value) {
        for (int x = 0; x < value.length(); ++x) {
            this.data[x + offset] = (byte) value.charAt(x);
        }
        this.data[offset + value.length()] = 0;
    }

    protected void setLong(int offset, long value) {
        this.buffer.clear();
        this.buffer.putLong(0, value);
        for (int x = 0; x < 4; ++x) {
            this.data[x + offset] = this.bdata[x];
        }
    }

    protected void setShort(int offset, long value) {
        this.buffer.clear();
        this.buffer.putShort(0, (short) value);
        for (int x = 0; x < 2; ++x) {
            this.data[x + offset] = this.bdata[x];
        }
    }

    protected void setCharacteristic(int key, boolean enable) {
        int offset = this.getInfo().getLocation("Characteristics");
        this.origch = enable ? (this.origch |= key) : (this.origch &= ~key);
        this.setShort(offset, this.origch);
    }

    public void setCompileTime(String date) {
        this.setCompileTime(CommonUtils.parseDate(date, "dd MMM yyyy HH:mm:ss"));
    }

    public void setCompileTime(long date) {
        int offset = this.getInfo().getLocation("TimeDateStamp");
        this.setLong(offset, date / 1000L);
    }

    public void setValueAt(String name, long value) {
        int offset = this.getInfo().getLocation(name);
        this.setLong(offset, value);
    }

    public void setImageSize(long size) {
        int offset = this.getInfo().getLocation("SizeOfImage");
        this.setLong(offset, size);
    }

    public void setRWXHint(boolean value) {
        this.setCharacteristic(32768, value);
    }

    protected void mask(int location, byte key) {
        StringBuilder check = new StringBuilder();
        while (this.data[location] != 0) {
            check.append((char) this.data[location]);
            byte[] arrby = this.data;
            int n = location++;
            arrby[n] = (byte) (arrby[n] ^ key);
        }
        byte[] arrby = this.data;
        int n = location;
        arrby[n] = (byte) (arrby[n] ^ key);
        if (check.toString().length() >= 63) {
            CommonUtils.print_error("String '" + check.toString() + "' is >=63 characters! Obfuscate WILL crash");
        }
    }

    public void obfuscate(boolean doit) {
        if (doit) {
            this._obfuscate();
            this._obfuscate_other();
        } else {
            this.setLong(this.getInfo().getLocation("NumberOfSymbols"), 0L);
        }
    }

    protected void _obfuscate_other() {
        int size = this.getInfo().get("e_lfanew");
        byte[] junk = CommonUtils.randomData(size - 64);
        this.setString(64, junk);
        size = this.getInfo().get("SizeOfHeaders") - this.getInfo().getLocation("HeaderSlack");
        junk = CommonUtils.randomData(size);
        this.setString(this.getInfo().getLocation("HeaderSlack"), junk);
    }

    protected void _obfuscate() {
        byte key = -1;
        this.setLong(this.getInfo().getLocation("NumberOfSymbols"), key);
        Iterator i = this.getInfo().stringIterator();
        while (i.hasNext()) {
            int location = (Integer) i.next();
            this.mask(location, key);
        }
    }

    public static void main(String[] args) {
        byte[] pe = CommonUtils.readFile(args[0]);
        PEEditor editor = new PEEditor(pe);
        editor.setCompileTime(System.currentTimeMillis() + 3600000L);
        editor.setImageSize(512000L);
        editor.setRWXHint(true);
        editor.obfuscate(false);
        PEParser parser = PEParser.load(editor.getImage());
        System.out.println(parser.toString());
    }
}

