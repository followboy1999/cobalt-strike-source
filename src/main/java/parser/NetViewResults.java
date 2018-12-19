package parser;

import common.CommonUtils;
import server.Resources;

import java.util.HashMap;
import java.util.Map;

public class NetViewResults
        extends Parser {
    private static final String IPADDR = "\\d+\\.\\d+\\.\\d+\\.\\d+";

    public NetViewResults(Resources r) {
        super(r);
    }

    @Override
    public boolean check(String text, int type) {
        return type == 24;
    }

    public Map host(String host, String name, String ver) {
        host = CommonUtils.trim(host);
        HashMap<String, String> temp = new HashMap<>();
        temp.put("address", host);
        temp.put("name", name);
        temp.put("os", "Windows");
        temp.put("version", ver);
        return temp;
    }

    public Map host(String host, String name) {
        host = CommonUtils.trim(host);
        HashMap<String, String> temp = new HashMap<>();
        temp.put("address", host);
        temp.put("name", name);
        return temp;
    }

    @Override
    public void parse(String text, String bid) {
        String[] lines = text.split("\n");
        boolean hastarget = false;
        for (String line : lines) {
            Map next;
            String key;
            String[] values = line.trim().split("\\s+");
            if (values.length >= 4 && values[1].matches(IPADDR)) {
                next = this.host(values[1], values[0], values[3]);
                key = CommonUtils.TargetKey(next);
                this.resources.call("targets.update", CommonUtils.args(key, next));
                hastarget = true;
                continue;
            }
            if (values.length != 2 || !values[1].matches(IPADDR)) continue;
            next = this.host(values[1], values[0]);
            key = CommonUtils.TargetKey(next);
            this.resources.call("targets.update", CommonUtils.args(key, next));
            hastarget = true;
        }
        if (hastarget) {
            this.resources.call("targets.push");
        }
    }
}

