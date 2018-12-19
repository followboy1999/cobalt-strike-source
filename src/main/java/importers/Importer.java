package importers;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class Importer {
    protected ImportHandler handler;
    protected HashSet hosts = new HashSet();

    public static List importers(ImportHandler handler) {
        LinkedList<Importer> temp = new LinkedList<>();
        temp.add(new FlatFile(handler));
        temp.add(new NmapXML(handler));
        return temp;
    }

    public Importer(ImportHandler handler) {
        this.handler = handler;
    }

    public void host(String host, String description, String os, double ver) {
        if (!this.hosts.contains(host = CommonUtils.trim(host))) {
            this.handler.host(host, description, os, ver);
            this.hosts.add(host);
        }
    }

    public void service(String host, String port, String description) {
        host = CommonUtils.trim(host);
        this.handler.service(host, port, description);
    }

    public abstract boolean parse(File var1) throws Exception;

    public boolean process(File file) {
        try {
            if (this.parse(file)) {
                return true;
            }
        } catch (Exception ex) {
            MudgeSanity.logException("import " + file, ex, false);
        }
        return false;
    }
}

