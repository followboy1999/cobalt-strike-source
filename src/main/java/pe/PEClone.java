package pe;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.io.FileInputStream;

public class PEClone {
    public void start(String file) {
        try {
            this._start(file);
        } catch (Exception ex) {
            MudgeSanity.logException("Error cloning headers of " + file, ex, false);
        }
    }

    public void out(String text) {
        System.out.println(text);
    }

    public void set(String key, long value) {
        this.set(key, value + "");
    }

    public void set(String key, byte[] value) {
        if (value.length == 0) {
            return;
        }
        this.set(key, CommonUtils.toAggressorScriptHexString(value));
    }

    public void set(String key, String value) {
        if (value != null) {
            System.out.println("\tset " + key + " \"" + value + "\";");
        }
    }

    public void _start(String file) throws Exception {
        File temp = new File(file);
        PEParser parser = PEParser.load(new FileInputStream(temp));
        this.out("# ./peclone " + new File(file).getName());
        this.out("stage {");
        this.set("checksum      ", parser.get("Checksum"));
        this.set("compile_time  ", CommonUtils.formatDateAny("dd MMM yyyy HH:mm:ss", parser.getDate("TimeDateStamp").getTime()));
        this.set("entry_point   ", parser.get("AddressOfEntryPoint"));
        if (parser.get("SizeOfImage") > 307200) {
            this.set("image_size_x86", parser.get("SizeOfImage"));
            this.set("image_size_x64", parser.get("SizeOfImage"));
        }
        this.set("name          ", parser.getString("Export.Name"));
        this.set("rich_header   ", parser.getRichHeader());
        this.out("}");
    }
}

