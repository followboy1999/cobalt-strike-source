package parser;

import common.BeaconEntry;
import common.CommonUtils;
import common.RegexParser;
import server.Resources;
import server.ServerUtils;

public class DcSyncCredentials
        extends Parser {
    public DcSyncCredentials(Resources r) {
        super(r);
    }

    @Override
    public boolean check(String text, int type) {
        return this.isOutput(type) && text.contains("\n** SAM ACCOUNT **") && text.contains("will be the domain");
    }

    @Override
    public void parse(String text, String bid) {
        String user = "";
        String domain = "";
        String hash2 = "";
        BeaconEntry entry = ServerUtils.getBeacon(this.resources, bid);
        if (entry == null) {
            return;
        }
        text = CommonUtils.strrep(text, "\r", "");
        String[] lines = text.split("\n");
        for (int x = 0; x < lines.length; ++x) {
            RegexParser parser = new RegexParser(lines[x]);
            if (parser.matches(".*?'(.*)' will be the domain.*")) {
                domain = parser.group(1);
                continue;
            }
            lines[x] = CommonUtils.strrep(lines[x], " ", "");
            lines[x] = CommonUtils.strrep(lines[x], "\t", "");
            int index = lines[x].indexOf(":");
            if (index <= 0 || index + 1 >= lines[x].length()) continue;
            String key = lines[x].substring(0, index);
            String value = lines[x].substring(index + 1);
            if ("SAMUsername".equals(key)) {
                user = value;
                continue;
            }
            if (!"HashNTLM".equals(key)) continue;
            hash2 = value;
        }
        if (!("".equals(user) || "".equals(domain) || "".equals(hash2))) {
            ServerUtils.addCredential(this.resources, user, hash2, domain, "dcsync", entry.getInternal());
            this.resources.call("credentials.push");
        }
    }
}

