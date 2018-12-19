package beacon;

import common.BeaconEntry;
import common.CommonUtils;
import common.StringStack;

public class Registry {
    public static final short HKLM = 0;
    public static final short HKCR = 1;
    public static final short HKCC = 2;
    public static final short HKCU = 3;
    public static final short HKU = 4;
    private static String[] shortNames = new String[]{"HKLM\\", "HKCR\\", "HKCC\\", "HKCU\\", "HKU\\"};
    private static String[] longNames = new String[]{"HKEY_LOCAL_MACHINE\\", "HKEY_CLASSES_ROOT\\", "HKEY_CURRENT_CONFIG\\", "HKEY_CURRENT_USER\\", "HKEY_USERS\\"};
    public static final short KEY_WOW64_64KEY = 256;
    public static final short KEY_WOW64_32KEY = 512;
    protected String pathv;
    protected String value = "";
    protected short hive = (short) -1;
    protected String arch;
    protected boolean hasvalue;

    protected void parseHive(String[] tryme) {
        for (int x = 0; x < tryme.length; ++x) {
            if (!this.pathv.startsWith(tryme[x])) continue;
            this.pathv = this.pathv.substring(tryme[x].length());
            this.hive = (short) x;
            break;
        }
    }

    public String getPath() {
        return this.pathv;
    }

    public String getValue() {
        return this.value;
    }

    public short getHive() {
        return this.hive;
    }

    public Registry(String arch, String path, boolean hasvalue) {
        this.arch = arch;
        this.hasvalue = hasvalue;
        if (hasvalue) {
            StringStack parser = new StringStack(path, " ");
            this.value = parser.pop();
            this.pathv = parser.toString();
        } else {
            this.pathv = path;
        }
        this.parseHive(shortNames);
        this.parseHive(longNames);
    }

    public short getFlags(BeaconEntry entry) {
        if (entry != null && "x86".equals(this.arch) && !entry.is64()) {
            CommonUtils.print_stat("Windows 2000 flag rule for " + entry);
            return 0;
        }
        if ("x86".equals(this.arch)) {
            return 512;
        }
        return 256;
    }

    public boolean isValid() {
        return this.getError() == null;
    }

    public String getError() {
        if (this.hasvalue && ("".equals(this.value) || "".equals(this.pathv))) {
            return "specify a value name too (e.g., HKLM\\foo\\bar Baz)";
        }
        if (this.hive == -1) {
            return "path must start with HKLM, HKCR, HKCC, HKCU, or HKU";
        }
        return null;
    }

    public String toString() {
        if (!this.isValid()) {
            return "[invalid]";
        }
        if ("".equals(this.value)) {
            return shortNames[this.getHive()] + this.pathv + " (" + this.arch + ")";
        }
        return shortNames[this.getHive()] + this.pathv + " /v " + this.value + " (" + this.arch + ")";
    }
}

