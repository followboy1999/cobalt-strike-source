package encoders;

import common.CommonUtils;
import common.Packer;

public class Transforms {
    public static byte[] toVeil(byte[] data) {
        Packer output = new Packer();
        for (byte aData : data) {
            output.addString("\\x");
            String f = Integer.toString(aData & 255, 16);
            if (f.length() == 2) {
                output.addString(f);
                continue;
            }
            output.addString("0" + f);
        }
        return output.getBytes();
    }

    public static String toArray(byte[] data) {
        Packer output = new Packer();
        for (int x = 0; x < data.length; ++x) {
            output.addString("0x");
            String f = Integer.toString(data[x] & 255, 16);
            if (f.length() == 2) {
                output.addString(f);
            } else {
                output.addString("0" + f);
            }
            if (x >= data.length - 1) continue;
            output.addString(", ");
        }
        return CommonUtils.bString(output.getBytes());
    }

    public static byte[] toC(byte[] data) {
        Packer output = new Packer();
        output.addString("/* length: " + data.length + " bytes */\n");
        output.addString("unsigned char buf[] = \"" + CommonUtils.bString(Transforms.toVeil(data)) + "\";\n");
        return output.getBytes();
    }

    public static byte[] toPerl(byte[] data) {
        Packer output = new Packer();
        output.addString("# length: " + data.length + " bytes\n");
        output.addString("$buf = \"" + CommonUtils.bString(Transforms.toVeil(data)) + "\";\n");
        return output.getBytes();
    }

    public static byte[] toPython(byte[] data) {
        Packer output = new Packer();
        output.addString("# length: " + data.length + " bytes\n");
        output.addString("buf = \"" + CommonUtils.bString(Transforms.toVeil(data)) + "\"\n");
        return output.getBytes();
    }

    public static byte[] toJava(byte[] data) {
        Packer output = new Packer();
        output.addString("/* length: " + data.length + " bytes */\n");
        output.addString("byte buf[] = new byte[] { " + Transforms.toArray(data) + " };\n");
        return output.getBytes();
    }

    public static byte[] toCSharp(byte[] data) {
        Packer output = new Packer();
        output.addString("/* length: " + data.length + " bytes */\n");
        output.addString("byte[] buf = new byte[" + data.length + "] { " + Transforms.toArray(data) + " };\n");
        return output.getBytes();
    }

    public static String toVBA(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 10);
        r.append("Array(");
        for (int x = 0; x < data.length; ++x) {
            r.append(data[x]);
            if (x > 0 && x % 40 == 0 && x + 1 < data.length) {
                r.append(", _\n");
                continue;
            }
            if (x + 1 >= data.length) continue;
            r.append(",");
        }
        r.append(")");
        return r.toString();
    }
}

