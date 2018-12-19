package importers;

import common.CommonUtils;

import java.io.File;
import java.util.Iterator;

public class TestImporters implements ImportHandler {
    public TestImporters(File target) {
        this.go(target);
    }

    public void go(File target) {
        Importer next;
        Iterator i = Importer.importers(this).iterator();
        while (i.hasNext() && !(next = (Importer) i.next()).process(target)) {
        }
        CommonUtils.print_info("Done!");
    }

    @Override
    public void host(String address, String name, String os, double ver) {
        StringBuilder result = new StringBuilder();
        result.append("host: ").append(address);
        if (name != null) {
            result.append(" / ").append(name);
        }
        if (os != null) {
            result.append(" (").append(os).append(" ").append(ver).append(")");
        }
        CommonUtils.print_good(result.toString());
    }

    @Override
    public void service(String address, String port, String description) {
        if (description != null) {
            CommonUtils.print_info(address + ":" + port + " - " + description);
        } else {
            CommonUtils.print_info(address + ":" + port);
        }
    }

    public static void main(String[] args) {
        File temp = new File(args[0]);
        new TestImporters(temp);
    }
}

