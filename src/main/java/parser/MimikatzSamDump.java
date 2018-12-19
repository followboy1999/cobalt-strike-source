package parser;

import common.BeaconEntry;
import common.CommonUtils;
import server.Resources;
import server.ServerUtils;

public class MimikatzSamDump
        extends Parser {
    public MimikatzSamDump(Resources r) {
        super(r);
    }

    @Override
    public boolean check(String text, int type) {
        return this.isOutput(type) && text.indexOf("RID") > 0 && text.indexOf("User : ") > 0 && text.indexOf("NTLM :") > 0 && text.indexOf("LM") > 0;
    }

    @Override
    public void parse(String text, String bid) {
        String user = "";
        String hash2 = "";
        BeaconEntry entry = ServerUtils.getBeacon(this.resources, bid);
        if (entry == null) {
            return;
        }
        text = CommonUtils.strrep(text, "\r", "");
        String[] lines = text.split("\n");
        for (String original : lines) {
            int index = original.indexOf(":");
            if (index <= 0 || index + 1 >= original.length()) continue;
            String key = original.substring(0, index);
            String value = original.substring(index + 1);
            key = CommonUtils.strrep(key, " ", "");
            key = CommonUtils.strrep(key, "\t", "");
            value = value.trim();
            if ("User".equals(key)) {
                user = value;
                continue;
            }
            if (!"NTLM".equals(key) || "".equals(value)) continue;
            ServerUtils.addCredential(this.resources, user, value, entry.getComputer(), "mimikatz", entry.getInternal());
        }
        this.resources.call("credentials.push");
    }
}

