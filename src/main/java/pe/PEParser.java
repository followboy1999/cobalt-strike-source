package pe;

import common.AssertUtils;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class PEParser {
    protected DataInputStream content;
    protected byte[] bdata = new byte[8];
    protected ByteBuffer buffer;
    protected HashMap<String, Object> values = new HashMap<>();
    protected byte[] original;
    protected Stack<DataInputStream> frames = new Stack<>();
    protected HashMap<String, Object> locations = new HashMap<>();
    protected LinkedList<Object> strings = new LinkedList<>();
    protected boolean procassembly = false;

    public boolean isProcessAssembly() {
        return this.procassembly;
    }

    protected void parseDirectory(int x) throws IOException {
        long va = this.readLong();
        long size = this.readLong();
        this.put("DataDirectory." + x + ".VirtualAddress", va);
        this.put("DataDirectory." + x + ".Size", size);
    }

    protected void parseFunctionNameHint(int real, int base, int next, LinkedList<String> items) throws IOException {
        this.jump(next - base + real);
        int hint = this.readShort();
        String name = this.readString();
        items.add(name + "@" + hint);
        this.complete();
    }

    protected List parseFunctionNameList(int ptr, int base, int real) throws IOException {
        LinkedList<String> items = new LinkedList<>();
        this.jump(ptr - base + real);
        do {
            if (this.is64()) {
                long next = this.readQWord();
                if ((next & Long.MIN_VALUE) == Long.MIN_VALUE) {
                    items.add("<ordinal>@" + (next & Long.MAX_VALUE));
                    continue;
                }
                if (next <= 0L) break;
                this.parseFunctionNameHint(real, base, (int) next, items);
                continue;
            }
            int next = this.readInt();
            if ((next & Integer.MIN_VALUE) == Integer.MIN_VALUE) {
                items.add("<ordinal>@" + (next & Integer.MAX_VALUE));
                continue;
            }
            if (next <= 0) break;
            this.parseFunctionNameHint(real, base, next, items);
        } while (true);
        this.complete();
        return items;
    }

    protected boolean parseImport(int x) throws IOException {
        int real = this.dirEntry(1);
        int base = this.get("DataDirectory.1.VirtualAddress");
        long RVAFunctionNameList = this.readInt();
        this.consume(8);
        long RVAModuleName = this.readInt();
        long RVAFunctionAddressList = this.readInt();
        if (RVAFunctionNameList == 0L && RVAModuleName == 0L && RVAFunctionAddressList == 0L) {
            return false;
        }
        this.put("Import." + x + ".RVAFunctionNameList", RVAFunctionNameList);
        this.put("Import." + x + ".RVAFunctionNameList.X", this.parseFunctionNameList((int) RVAFunctionNameList, base, real));
        this.put("Import." + x + ".RVAModuleName", RVAModuleName);
        this.put("Import." + x + ".RVAModuleName.X", this.getStringFromPointer((int) RVAModuleName, base, real));
        this.put("Import." + x + ".RVAFunctionAddressList", RVAFunctionAddressList);
        return true;
    }

    public int getPointerForLocation(int sectionNo, int where) {
        int real = this.dirEntry(sectionNo);
        int base = this.get("DataDirectory." + sectionNo + ".VirtualAddress");
        return where - real + base;
    }

    protected String getStringFromPointer(int ptr, int base, int real) throws IOException {
        this.jump(ptr - base + real);
        String res = this.readString();
        this.complete();
        return res;
    }

    public List getExportedFunctions() {
        return (List) this.values.get("Export.FunctionNames");
    }

    public int getFunctionOffset(String wanted) {
        List funcs = this.getExportedFunctions();
        List addrs = (List) this.values.get("Export.FunctionAddressesFixed");
        Iterator i = funcs.iterator();
        Iterator j = addrs.iterator();
        while (i.hasNext() && j.hasNext()) {
            String func = (String) i.next();
            Long addr = (Long) j.next();
            if (!wanted.equals(func)) continue;
            return (int) addr.longValue();
        }
        return -1;
    }

    protected void parseExport() throws IOException {
        int x;
        int real = this.dirEntry(0);
        int base = this.get("DataDirectory.0.VirtualAddress");
        this.consume(12);
        this.report("Export.Name");
        this.put("Export.Name", this.getStringFromPointer(this.readInt(), base, real));
        this.put("Export.Base", this.readInt());
        this.put("Export.NumberOfFunctions", this.readInt());
        this.put("Export.NumberOfNames", this.readInt());
        this.put("Export.AddressOfFunctions", this.readInt());
        this.put("Export.AddressOfNames", this.readInt());
        this.put("Export.AddressOfNameOridinals", this.readInt());
        this.jump(this.get("Export.AddressOfNames") - base + real);
        this.jump(this.readInt() - base + real);
        LinkedList<Object> items = new LinkedList<>();
        for (x = 0; x < this.get("Export.NumberOfNames"); ++x) {
            items.add(this.readString());
        }
        this.put("Export.FunctionNames", items);
        this.complete();
        this.complete();
        this.jump(this.get("Export.AddressOfFunctions") - base + real);
        items = new LinkedList<>();
        for (x = 0; x < this.get("Export.NumberOfNames"); ++x) {
            items.add(this.readLong());
        }
        this.put("Export.FunctionAddresses", items);
        this.complete();
        this.jump(this.get("Export.AddressOfFunctions") - base + real);
        items = new LinkedList<>();
        for (x = 0; x < this.get("Export.NumberOfNames"); ++x) {
            items.add(this.fixAddress(this.readLong()));
        }
        this.put("Export.FunctionAddressesFixed", items);
        this.complete();
    }

    public long fixAddress(long rva_addr) {
        for (Object o : this.SectionsTable()) {
            String section = o + "";
            if (!this.inSection(section, rva_addr)) continue;
            return rva_addr - (long) this.sectionAddress(section) + (long) this.sectionStart(section);
        }
        return -1L;
    }

    public static PEParser load(InputStream i) {
        return new PEParser(i);
    }

    public static PEParser load(byte[] d) {
        return new PEParser(d);
    }

    protected PEParser(InputStream i) {
        this(CommonUtils.readAll(i));
    }

    protected void jump(long offset) throws IOException {
        this.frames.push(this.content);
        this.content = new DataInputStream(new ByteArrayInputStream(this.original));
        if (offset > 0L) {
            this.consume((int) offset);
        }
    }

    protected void complete() throws IOException {
        this.content.close();
        this.content = this.frames.pop();
    }

    public long checksum() {
        PEImageChecksum summer = new PEImageChecksum(this.getLocation("CheckSum"));
        summer.update(this.original, 0, this.original.length);
        return summer.getValue();
    }

    protected PEParser(byte[] data) {
        this.original = data;
        this.buffer = ByteBuffer.wrap(this.bdata);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.content = new DataInputStream(new ByteArrayInputStream(data));
        try {
            this.parse();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void consume(int x) throws IOException {
        this.content.skipBytes(x);
    }

    protected int readInt() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 4);
        return (int) this.buffer.getLong(0);
    }

    protected long readLong() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 4);
        return this.buffer.getLong(0);
    }

    protected long readQWord() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 8);
        return this.buffer.getLong(0);
    }

    protected char readChar() throws IOException {
        return (char) this.content.readByte();
    }

    protected char readChar(DataInputStream c) throws IOException {
        return (char) c.readByte();
    }

    protected int readShort() throws IOException {
        this.content.read(this.bdata, 0, 2);
        return this.buffer.getShort(0) & 65535;
    }

    protected String readString() throws IOException {
        char next;
        this.string();
        StringBuilder r = new StringBuilder();
        while ((next = this.readChar()) > '\u0000') {
            r.append(next);
        }
        if (r.toString().startsWith("_ReflectiveLoader") || r.toString().startsWith("ReflectiveLoader")) {
            this.strings.removeLast();
        }
        if (r.toString().contains("CorExeMain")) {
            this.procassembly = true;
        }
        return r.toString();
    }

    protected String readString(int len) throws IOException {
        this.string();
        StringBuilder r = new StringBuilder();
        for (int x = 0; x < len; ++x) {
            char next = this.readChar();
            if (next > '\u0000') {
                r.append(next);
            }
        }
        if (r.toString().startsWith("_ReflectiveLoader") || r.toString().startsWith("ReflectiveLoader")) {
            this.strings.removeLast();
        }
        if (r.toString().contains("CorExeMain")) {
            this.procassembly = true;
        }
        return r.toString();
    }

    protected void put(String key, long l) {
        this.values.put(key, l);
    }

    protected void put(String key, List moo) {
        this.values.put(key, moo);
    }

    protected void put(String key, String value) {
        this.values.put(key, value);
    }

    protected void put(String key, Date value) {
        this.values.put(key, value);
    }

    protected void error(String error) {
        throw new RuntimeException(error);
    }

    protected void header(String name, int magic) throws Exception {
        this.report("header." + name);
        int magicv = this.readShort();
        if (magicv != magic) {
            this.error("Header " + name + " Magic Failed: " + magicv + " expected (" + magic + ")");
        }
    }

    public int get(String key) {
        Long l = (Long) this.values.get(key);
        if (l == null) {
            return 0;
        }
        return (int) l.longValue();
    }

    public Date getDate(String key) {
        return (Date) this.values.get(key);
    }

    public String getString(String key) {
        return (String) this.values.get(key);
    }

    protected void put(String section, String key, long value) {
        this.values.put(section + "." + key, value);
    }

    protected void put(String section, String key, String value) {
        this.values.put(section + "." + key, new Long(value));
    }

    protected void readCharacteristics(String name) throws IOException {
        long c = this.readLong();
        LinkedList<String> r = new LinkedList<>();
        if ((c & 32L) == 32L) {
            r.add("Code");
        }
        if ((c & 64L) == 64L) {
            r.add("Initialized Data");
        }
        if ((c & 128L) == 128L) {
            r.add("Uninitialized Data");
        }
        if ((c & 0x4000000L) == 0x4000000L) {
            r.add("Section cannot be cached");
        }
        if ((c & 0x8000000L) == 0x8000000L) {
            r.add("Section is not pageable");
        }
        if ((c & 0x10000000L) == 0x10000000L) {
            r.add("Section is shared");
        }
        if ((c & 0x20000000L) == 0x20000000L) {
            r.add("Executable");
        }
        if ((c & 0x40000000L) == 0x40000000L) {
            r.add("Readable");
        }
        if ((c & 0x80000000L) == 0x80000000L) {
            r.add("Writable");
        }
        r.add("0x" + Long.toString(c, 16));
        this.values.put(name + ".Characteristics", r);
    }

    protected Date readDate() throws IOException {
        return new Date(this.readLong() * 1000L);
    }

    protected void parseSection() throws Exception {
        String name = this.readString(8);
        this.append("SectionsTable", name);
        this.put(name, "VirtualSize", this.readInt());
        this.put(name, "VirtualAddress", this.readInt());
        this.put(name, "SizeOfRawData", this.readInt());
        this.put(name, "PointerToRawData", this.readInt());
        this.consume(12);
        this.readCharacteristics(name);
    }

    protected void append(String name, String value) {
        this.values.computeIfAbsent(name, k -> new LinkedList<>());
        LinkedList<String> temp = (LinkedList) this.values.get(name);
        temp.add(value);
    }

    public int sectionStart(String name) {
        return this.get(name + ".PointerToRawData");
    }

    public int sectionSize(String name) {
        return this.get(name + ".SizeOfRawData");
    }

    public int sectionAddress(String name) {
        return this.get(name + ".VirtualAddress");
    }

    public int sectionEnd(String name) {
        return this.get(name + ".VirtualAddress") + this.get(name + ".VirtualSize");
    }

    protected boolean inSection(String name, long rva_addr) {
        long base = this.sectionAddress(name);
        long size = this.get(name + ".VirtualSize");
        return rva_addr >= base && rva_addr < base + size;
    }

    public List SectionsTable() {
        return (List) this.values.get("SectionsTable");
    }

    protected int dirEntry(int x) {
        int dirRva = this.get("DataDirectory." + x + ".VirtualAddress");
        for (Object o : this.SectionsTable()) {
            String section = (String) o;
            int addr = this.sectionAddress(section);
            int max = this.sectionSize(section);
            if (dirRva < addr || dirRva >= addr + max) continue;
            return this.sectionStart(section) + (dirRva - addr);
        }
        throw new RuntimeException("Directory entry: " + x + "@" + dirRva + " not found");
    }

    public boolean is64() {
        return this.get("Machine") == 34404;
    }

    protected void parse64() throws Exception {
        this.header("Optional", 523);
        this.consume(14);
        this.report("AddressOfEntryPoint");
        this.put("AddressOfEntryPoint", this.readInt());
        this.consume(4);
        this.put("ImageBase", this.readQWord());
        this.report("SectionAlignment");
        this.put("SectionAlignment", this.readInt());
        this.put("FileAlignment", this.readInt());
        this.consume(8);
        this.put("MajorSubSystemVersion", this.readShort());
        this.consume(6);
        this.report("SizeOfImage");
        this.put("SizeOfImage", this.readInt());
        this.put("SizeOfHeaders", this.readInt());
        this.report("CheckSum");
        this.put("CheckSum", this.readInt());
        this.put("Subsystem", this.readShort());
        this.put("DllCharacteristics", this.readShort());
        this.consume(32);
        this.report("LoaderFlags");
        this.put("LoaderFlags", this.readInt());
        this.put("NumberOfRvaAndSizes", this.readInt());
    }

    protected void parse32() throws Exception {
        this.header("Optional", 267);
        this.consume(14);
        this.report("AddressOfEntryPoint");
        this.put("AddressOfEntryPoint", this.readInt());
        this.consume(8);
        this.put("ImageBase", this.readInt());
        this.report("SectionAlignment");
        this.put("SectionAlignment", this.readInt());
        this.put("FileAlignment", this.readInt());
        this.consume(8);
        this.put("MajorSubSystemVersion", this.readShort());
        this.consume(6);
        this.report("SizeOfImage");
        this.put("SizeOfImage", this.readInt());
        this.put("SizeOfHeaders", this.readInt());
        this.report("CheckSum");
        this.put("CheckSum", this.readInt());
        this.put("Subsystem", this.readShort());
        this.put("DllCharacteristics", this.readShort());
        this.consume(16);
        this.report("LoaderFlags");
        this.put("LoaderFlags", this.readInt());
        this.put("NumberOfRvaAndSizes", this.readInt());
    }

    public int here() throws IOException {
        return this.original.length - this.content.available();
    }

    public void string() {
        try {
            int offset = this.here();
            this.strings.add(offset);
        } catch (Exception ex) {
            MudgeSanity.logException("string", ex, false);
        }
    }

    public void report(String name) {
        try {
            int offset = this.here();
            this.locations.put(name, offset);
        } catch (Exception ex) {
            MudgeSanity.logException("report: " + name, ex, false);
        }
    }

    public Iterator stringIterator() {
        return this.strings.iterator();
    }

    public int getLocation(String name) {
        if (!this.locations.containsKey(name)) {
            throw new IllegalArgumentException("No location for '" + name + "'");
        }
        int value = (Integer) this.locations.get(name);
        AssertUtils.Test(value >= 60, name + " (offset: " + value + ") Reflective Loader bootstrap region");
        return value;
    }

    public int getRichHeaderSize() {
        return this.get("e_lfanew") - 128;
    }

    public byte[] getRichHeader() {
        if (this.getRichHeaderSize() <= 0) {
            return new byte[0];
        }
        return Arrays.copyOfRange(this.original, 128, this.get("e_lfanew"));
    }

    protected void parse() throws Exception {
        int x;
        this.header("e_magic", 23117);
        this.consume(58);
        this.report("e_lfanew");
        this.put("e_lfanew", this.readInt());
        this.jump(this.get("e_lfanew"));
        this.header("PE", 17744);
        this.consume(2);
        this.put("Machine", this.readShort());
        this.put("Sections", this.readShort());
        this.report("TimeDateStamp");
        this.put("TimeDateStamp", this.readDate());
        this.put("PointerToSymbolTable", this.readInt());
        this.report("NumberOfSymbols");
        this.put("NumberOfSymbols", this.readInt());
        this.put("SizeOfOptionalHeader", this.readShort());
        this.report("Characteristics");
        this.put("Characteristics", this.readShort());
        if (this.is64()) {
            this.parse64();
        } else {
            this.parse32();
        }
        for (x = 0; x < this.get("NumberOfRvaAndSizes"); ++x) {
            this.parseDirectory(x);
        }
        for (x = 0; x < this.get("Sections"); ++x) {
            this.parseSection();
        }
        this.report("HeaderSlack");
        this.complete();
        this.jump(this.dirEntry(1));
        x = 0;
        while (this.parseImport(x)) {
            ++x;
        }
        this.complete();
        if (this.get("DataDirectory.0.VirtualAddress") != 0) {
            this.jump(this.dirEntry(0));
            this.parseExport();
            this.complete();
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("Key                                Value\n");
        out.append("---                                -----\n");
        for (Map.Entry<String, Object> entry : new TreeMap<>(this.values).entrySet()) {
            StringBuilder key = new StringBuilder(entry.getKey());
            while (key.length() < 35) {
                key.append(" ");
            }
            out.append(key);
            Object value = entry.getValue();
            if (value instanceof Long) {
                long lvalue = (Long) entry.getValue();
                StringBuilder svalue = new StringBuilder("0x" + Long.toString(lvalue, 16));
                while (svalue.length() < 12) {
                    svalue.append(" ");
                }
                svalue.append(lvalue);
                out.append(svalue);
                out.append("\n");
            } else if (value instanceof String) {
                out.append(entry.getValue()).append("\n");
            } else if (entry.getValue() instanceof List) {
                out.append(entry.getValue()).append("\n");
            } else if (entry.getValue() instanceof Date) {
                long lvalue = ((Date) entry.getValue()).getTime() / 1000L;
                StringBuilder builder = new StringBuilder("0x" + Long.toString(lvalue, 16));
                while (builder.length() < 12) {
                    builder.append(" ");
                }
                builder.append(lvalue);
                while (builder.length() < 32) {
                    builder.append(" ");
                }
                builder.append(CommonUtils.formatDateAny("dd MMM yyyy HH:mm:ss", lvalue * 1000L));
                out.append(builder);
                out.append("\n");
            }
        }
        return out.toString();
    }

    public static void dump(String[] args) throws Exception {
        File temp = new File(args[1]);
        PEParser parser = PEParser.load(new FileInputStream(temp));
        System.out.println(parser.toString());
        System.out.println("Checksum: " + parser.checksum());
        System.out.println("\n\nLocations:\n----------");
        for (Map.Entry<String, Object> entry : parser.locations.entrySet()) {
            StringBuilder key = new StringBuilder(entry.getKey());
            while (key.length() < 31) {
                key.append(" ");
            }
            System.out.println(key + " " + entry.getValue());
        }
    }

    public static void stage(String[] args) {
        PEClone clone = new PEClone();
        clone.start(args[0]);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            CommonUtils.print_info("Cobalt Strike PE Parser. Options:\n\t./peclone [file]\n\t\tDump PE headers as a Malleable PE stage block\n\t./peclone dump [file]\n\t\tRun Cobalt Strike's PE parser against the file");
        } else if (args[0].equals("dump") && args.length == 2) {
            PEParser.dump(args);
        } else {
            PEParser.stage(args);
        }
    }
}

