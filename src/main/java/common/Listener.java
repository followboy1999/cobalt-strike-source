package common;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import dialog.DialogUtils;

import java.io.Serializable;
import java.util.*;

public class Listener implements Serializable {
    protected String payload;
    protected String name;
    protected String host;
    protected int port;
    protected String beacons;
    protected boolean dns;
    protected String status;
    protected ListenerConfig config;

    public Listener(Map options, boolean dns) {
        this.payload = (String) options.get("payload");
        this.name = (String) options.get("name");
        this.host = (String) options.get("host");
        this.port = Integer.parseInt(DialogUtils.string(options, "port"));
        this.beacons = (String) options.get("beacons");
        this.status = (String) options.get("status");
        this.config = (ListenerConfig) options.get("config");
        this.dns = dns;
    }

    public ListenerConfig getConfig() {
        return this.config;
    }

    public boolean hasPublicStage() {
        return this.config.hasPublicStage();
    }

    public static Listener getListener(String name) {
        GlobalDataManager data = GlobalDataManager.getGlobalDataManager();
        Map listeners = (Map) data.get("listeners", new HashMap());
        boolean dns = false;
        if (name.endsWith(" (DNS)")) {
            dns = true;
            name = name.substring(0, name.length() - 6);
        }
        if (!listeners.containsKey(name)) {
            throw new RuntimeException("Listener '" + name + "' not found.");
        }
        return new Listener((Map) listeners.get(name), dns);
    }

    public static Map filterSMB(Map options) {
        Iterator i = options.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry next = (Map.Entry) i.next();
            Map val = (Map) next.getValue();
            if (!"windows/beacon_smb/bind_pipe".equals(val.get("payload"))) continue;
            i.remove();
        }
        return options;
    }

    public static List getListenerNames(Map listeners) {
        LinkedList<String> results = new LinkedList<>();
        for (Object o : listeners.values()) {
            Listener l = new Listener((Map) o, false);
            results.add(l.getName());
            if (!"windows/beacon_dns/reverse_http".equals(l.getPayload())) continue;
            results.add(l.getName() + " (DNS)");
        }
        return results;
    }

    public static boolean isForeignListener(Map listener) {
        Listener easy = new Listener(listener, false);
        return easy.isForeign();
    }

    public static boolean isUsableListener(Map listener) {
        Listener easy = new Listener(listener, false);
        return !easy.isEgress() || easy.hasPublicStage();
    }

    public static Map filterListenersWithoutStage(Map listeners) {
        listeners = new HashMap(listeners);
        Iterator i = listeners.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry next = (Map.Entry) i.next();
            if (Listener.isUsableListener((Map) next.getValue())) continue;
            i.remove();
        }
        return listeners;
    }

    public static List filterListenersWithoutStage(List listeners) {
        listeners = new LinkedList(listeners);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            if (Listener.isUsableListener((Map) i.next())) continue;
            i.remove();
        }
        return listeners;
    }

    public static List getListenerNamesWithSMB(DataManager datal) {
        GlobalDataManager data = GlobalDataManager.getGlobalDataManager();
        Map listeners = Listener.filterSMB((Map) data.get("listeners", new HashMap()));
        listeners.putAll((Map) datal.get("listeners", new HashMap()));
        return Listener.getListenerNames(Listener.filterListenersWithoutStage(listeners));
    }

    public static List<String> getListenerNamesForStagelessPayloads(DataManager datal) {
        List<Map<String, Object>> l = DataUtils.getListenerModel(datal);
        LinkedList<String> strings = new LinkedList<>();
        for (Map<String, Object> next : l) {
            if (!Listener.isForeignListener(next)) {
                String s = (String) next.get("name");
                strings.add(s);
            }
        }
        return strings;
    }

    public static List getListenerNamesNoSMB() {
        GlobalDataManager data = GlobalDataManager.getGlobalDataManager();
        Map listeners = Listener.filterSMB((Map) data.get("listeners", new HashMap()));
        return Listener.getListenerNames(Listener.filterListenersWithoutStage(listeners));
    }

    public static List getListenerNamesWithSMBLocal(DataManager datal) {
        return Listener.getListenerNames(Listener.filterListenersWithoutStage(datal.getMapSafe("listeners")));
    }

    public static boolean isListener(String listenerName) {
        if (listenerName.endsWith(" (DNS)")) {
            return Listener.isListener(listenerName.substring(0, listenerName.length() - 6));
        }
        GlobalDataManager data = GlobalDataManager.getGlobalDataManager();
        Map listeners = (Map) data.get("listeners", new HashMap());
        return listeners.containsKey(listenerName);
    }

    public int getPort() {
        return this.port;
    }

    public String getHost() {
        return this.host;
    }

    public String getUserAgent() {
        return this.config.getUserAgent();
    }

    public String getName() {
        return this.name;
    }

    public String[] getBeaconHosts() {
        return CommonUtils.toArray(this.beacons);
    }

    public String getDNSHost() {
        if (this.isForeign()) {
            return this.getHost();
        }
        return this.getBeaconHosts()[0];
    }

    public String getDNSStagerHost() {
        if (this.isForeign() || "".equals(this.config.getDNSSubhost())) {
            long nonce = CommonUtils.rand(16777215);
            return ".stage." + nonce + "." + this.getDNSHost();
        }
        return this.config.getDNSSubhost() + this.getDNSHost();
    }

    public boolean isStager(String name) {
        return this.payload.endsWith(name);
    }

    public String getStager() {
        if (this.isStager("reverse_tcp")) {
            return "reverse_tcp";
        }
        if (this.isStager("reverse_http")) {
            return "reverse_http";
        }
        if (this.isStager("reverse_https")) {
            return "reverse_https";
        }
        if (this.isStager("bind_pipe")) {
            return "bind_pipe";
        }
        if (this.isStager("bind_tcp")) {
            return "bind_tcp";
        }
        if (this.isStager("reverse_dns_txt")) {
            return "reverse_dns_txt";
        }
        return "unknown";
    }

    public Map toMap() {
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("payload", this.payload);
        temp.put("host", this.host);
        temp.put("port", this.port);
        temp.put("beacons", this.beacons);
        temp.put("useragent", this.config.getUserAgent());
        temp.put("dns", this.dns);
        temp.put("name", this.name);
        temp.put("status", this.status);
        return temp;
    }

    public boolean isForeign() {
        return this.payload.contains("foreign");
    }

    public String getPipe() {
        return CommonUtils.strrep(this.config.getStagerPipe(), "##", this.getPort() + "");
    }

    public String getPayload() {
        return this.payload;
    }

    public boolean isEgress() {
        return Listener.isEgressBeacon(this.getPayload());
    }

    public String toString() {
        if (this.isDNS()) {
            return this.getPayload() + " (" + this.getDNSHost() + " via DNS TXT)";
        }
        if (this.isStager("bind_pipe")) {
            return this.getPayload() + " (127.0.0.1:" + this.getPort() + ")";
        }
        return this.getPayload() + " (" + this.getHost() + ":" + this.getPort() + ")";
    }

    public String toString(String target) {
        if (this.isStager("bind_pipe")) {
            return this.getPayload() + " (\\\\" + target + "\\pipe\\" + this.getPipe() + ")";
        }
        return this.toString();
    }

    public boolean isDNS() {
        return this.dns || this.isStager("reverse_dns_txt");
    }

    public static boolean isEgressBeacon(String payload) {
        return "windows/beacon_http/reverse_http".equals(payload) ||
                "windows/beacon_dns/reverse_http".equals(payload) ||
                "windows/beacon_dns/reverse_dns_txt".equals(payload) ||
                "windows/beacon_https/reverse_https".equals(payload);
    }
}

