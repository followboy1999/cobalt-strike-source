package importers;

import common.CommonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FlatFile
        extends Importer {
    public FlatFile(ImportHandler handler) {
        super(handler);
    }

    public boolean isHostAndPort(String entry) {
        String[] parts = entry.split(":");
        if (parts.length == 2 && CommonUtils.isIP(parts[0])) {
            this.host(parts[0], null, null, 0.0);
            this.service(parts[0], parts[1], null);
            return true;
        }
        return false;
    }

    @Override
    public boolean parse(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String entry = reader.readLine();
        while (entry != null) {
            if (!(entry = entry.trim()).startsWith("# ")) {
                if (CommonUtils.isIP(entry)) {
                    this.host(entry, null, null, 0.0);
                } else if (!this.isHostAndPort(entry) && !"".equals(entry)) {
                    reader.close();
                    return false;
                }
            }
            entry = reader.readLine();
        }
        return true;
    }
}

