package parser;

import common.CommonUtils;
import common.RegexParser;
import server.Resources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ScanResults
        extends Parser {
    public ScanResults(Resources r) {
        super(r);
    }

    @Override
    public boolean check(String text, int type) {
        return type == 25;
    }

    public void addHost(Map hosts, String host) {
        if (hosts.containsKey(host = CommonUtils.trim(host))) {
            return;
        }
        HashMap<String, String> temp = new HashMap<>();
        temp.put("address", host);
        hosts.put(host, temp);
    }

    public Map service(String target, String port) {
        target = CommonUtils.trim(target);
        HashMap<String, String> temp = new HashMap<>();
        temp.put("address", target);
        temp.put("port", port);
        return temp;
    }

    public Map service(String target, String port, String banner) {
        target = CommonUtils.trim(target);
        HashMap<String, String> temp = new HashMap<>();
        temp.put("address", target);
        temp.put("port", port);
        temp.put("banner", banner);
        return temp;
    }

    @Override
    public void parse(String text, String bid) {
        String[] lines = text.split("\n");
        HashMap hosts = new HashMap();
        LinkedList<Map> services = new LinkedList<>();
        for (String line : lines) {
            String port;
            Map data;
            String host;
            RegexParser parser = new RegexParser(line);
            if (parser.matches("(.*?):(\\d+) \\((.*?)\\)")) {
                host = parser.group(1);
                port = parser.group(2);
                String banner = parser.group(3);
                this.addHost(hosts, host);
                services.add(this.service(host, port, banner));
            } else if (parser.matches("(.*?):(\\d+)")) {
                host = parser.group(1);
                port = parser.group(2);
                this.addHost(hosts, host);
                services.add(this.service(host, port));
            }
            if (parser.matches("(.*?):445 \\(platform: (\\d+) version: (.*?) name: (.*?) domain: (.*?)\\)")) {
                host = parser.group(1);
                String platform = parser.group(2);
                String version = parser.group(3);
                String name = parser.group(4);
                String domain = parser.group(5);
                Map data2 = (Map) hosts.get(host);
                if ("4.9".equals(version)) continue;
                data2.put("os", "Windows");
                data2.put("version", version);
                data2.put("name", name);
                continue;
            }
            if (!parser.matches("(.*?):22 \\((.*?)\\)")) continue;
            host = parser.group(1);
            String banner = parser.group(2).toLowerCase();
            if (banner.contains("debian")) {
                data = (Map) hosts.get(host);
                data.put("os", "Linux");
                continue;
            }
            if (banner.contains("ubuntu")) {
                data = (Map) hosts.get(host);
                data.put("os", "Linux");
                continue;
            }
            if (banner.contains("cisco")) {
                data = (Map) hosts.get(host);
                data.put("os", "Cisco IOS");
                continue;
            }
            if (banner.contains("freebsd")) {
                data = (Map) hosts.get(host);
                data.put("os", "FreeBSD");
                continue;
            }
            if (!banner.contains("openbsd")) continue;
            data = (Map) hosts.get(host);
            data.put("os", "OpenBSD");
        }
        Iterator i = hosts.values().iterator();
        while (i.hasNext()) {
            Map next = (Map) i.next();
            String key = CommonUtils.TargetKey(next);
            this.resources.call("targets.update", CommonUtils.args(key, next));
        }
        i = services.iterator();
        while (i.hasNext()) {
            Map next = (Map) i.next();
            String key = CommonUtils.ServiceKey(next);
            this.resources.call("services.update", CommonUtils.args(key, next));
        }
        this.resources.call("services.push");
        this.resources.call("targets.push");
    }
}

