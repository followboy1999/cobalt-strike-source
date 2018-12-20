package aggressor;

import beacon.BeaconCommands;
import beacon.BeaconExploits;
import common.*;
import dialog.DialogUtils;
import stagers.Stagers;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class DataUtils {
    protected static final Map<String, String> tokenCache = new HashMap<>();

    public static String getNick(DataManager data) {
        return data.getMapSafe("metadata").get("nick") + "";
    }

    public static long AdjustForSkew(DataManager data, long time) {
        long skew = CommonUtils.toLongNumber(data.getMapSafe("metadata").get("clockskew") + "", 0L);
        return time - skew;
    }

    public static boolean hasValidSSL(DataManager data) {
        return "true".equals(data.getMapSafe("metadata").get("validssl"));
    }

    public static boolean hasImportedPowerShell(DataManager data, String bid) {
        return DataUtils.getBeaconPowerShellCommands(data, bid).size() > 0;
    }

    public static void reportPowerShellImport(DataManager data, String bid, List<String> cmdlets) {
        data.put("cmdlets", bid, cmdlets);
    }

    public static Map<String, Object> getC2Info(DataManager data) {
        HashMap<String, Object> temp = new HashMap<>();
        for (Object o : data.getMapSafe("metadata").entrySet()) {
            Map.Entry next = (Map.Entry) o;
            if (!next.getKey().toString().startsWith("c2sample.")) continue;
            temp.put(next.getKey().toString().substring(9), next.getValue());
        }
        temp.put("callbacks", data.getListSafe("c2info"));
        return temp;
    }

    public static CodeSigner getSigner(DataManager data) {
        return (CodeSigner) data.getMapSafe("metadata").get("signer");
    }

    public static HashSet getUsers(DataManager data) {
        return data.getSetSafe("users");
    }

    public static long getTime(DataManager data) {
        return System.currentTimeMillis();
    }

    public static BeaconEntry getBeacon(DataManager data, String bid) {
        return DataUtils.getBeaconFromResult(DataUtils.getBeacons(data), bid);
    }

    public static List<String> getBeaconChain(DataManager data, String bid) {
        return DataUtils.getBeaconChain(data, bid, new LinkedList<>());
    }

    private static List<String> getBeaconChain(DataManager data, String bid, List<String> results) {
        BeaconEntry entry = DataUtils.getBeacon(data, bid);
        if (entry != null) {
            results.add(entry.getInternal());
        }
        if (entry.isLinked()) {
            return DataUtils.getBeaconChain(data, entry.getParentId(), results);
        }
        return results;
    }

    public static byte[] encodeForBeacon(DataManager data, String bid, String text) {
        BeaconEntry entry = DataUtils.getBeacon(data, bid);
        if (entry == null) {
            return CommonUtils.toBytes(text);
        }
        return CommonUtils.toBytes(text, entry.getCharset());
    }

    public static String decodeForBeacon(DataManager data, String bid, byte[] dataz) {
        BeaconEntry entry = DataUtils.getBeacon(data, bid);
        if (entry == null) {
            return CommonUtils.bString(dataz);
        }
        return CommonUtils.bString(dataz, entry.getCharset());
    }

    public static BeaconEntry getEgressBeacon(DataManager data, String bid) {
        BeaconEntry entry = DataUtils.getBeacon(data, bid);
        if (entry == null) {
            return null;
        }
        if (entry.isLinked()) {
            return DataUtils.getEgressBeacon(data, entry.getParentId());
        }
        return entry;
    }

    public static Map<String,BeaconEntry> getBeacons(DataManager data) {
        return (Map<String, BeaconEntry>)(Map) data.getMapSafe("beacons");
    }

    public static List<BeaconEntry> getBeaconChildren(DataManager data, String bid) {
        Iterator<Map.Entry<String, BeaconEntry>> i = DataUtils.getBeacons(data).entrySet().iterator();
        LinkedList<BeaconEntry> results = new LinkedList<>();
        while (i.hasNext()) {
            Map.Entry<String, BeaconEntry> next = i.next();
            BeaconEntry temp = next.getValue();
            if (!bid.equals(temp.getParentId())) continue;
            results.add(temp);
        }
        return results;
    }

    public static BeaconEntry getBeaconFromResult(Object result, String bid) {
        Map temp = (Map) result;
        if (temp.containsKey(bid)) {
            return (BeaconEntry) temp.get(bid);
        }
        return null;
    }

    public static List<Map<String, Object>> getBeaconModel(DataManager data) {
        return DataUtils.getBeaconModelFromResult(DataUtils.getBeacons(data));
    }

    public static List<Map<String, Object>> getBeaconModelFromResult(Map<String,BeaconEntry> resultz) {
        LinkedList<Map<String,Object>> result = new LinkedList<>();
        for (BeaconEntry entry : resultz.values()) {
            Map<String, Object> value = entry.toMap();
            if (entry.isEmpty()) {
                value.put("image", DialogUtils.TargetVisualizationSmall("unknown", 0.0, false, false));
            } else {
                value.put("image", DialogUtils.TargetVisualizationSmall(entry.getOperatingSystem().toLowerCase(), entry.getVersion(), entry.isAdmin(), !entry.isAlive()));
            }
            result.add(value);
        }
        return result;
    }

    public static List getSites(GenericDataManager data) {
        return data.getListSafe("sites");
    }

    public static List<String> getTargetNames(DataManager data) {
        return ((LinkedList<Map>) data.getListSafe("targets")).stream().map(entry -> (String) entry.get("name")).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedList::new));
    }

    public static List<Object> getListenerModel(GenericDataManager datag, DataManager datal) {
        Map a = Listener.filterSMB(datag.getMapSafe("listeners"));
        Map<String, Object> b = datal.getMapSafe("listeners");
        a.putAll(b);
        return new LinkedList<Object>(a.values());
    }

    public static List getUsableListenerModel(GenericDataManager datag, DataManager datal) {
        return Listener.filterListenersWithoutStage(DataUtils.getListenerModel(datag, datal));
    }

    public static String getAddressFor(DataManager data, String addr) {
        List<Map> hosts = data.getListSafe("targets");
        for (Map next : hosts) {
            if (!addr.equals(next.get("address"))) continue;
            String name = (String) next.get("name");
            if (name != null && !"".equals(name)) {
                return name;
            }
            return addr;
        }
        return addr;
    }

    public static String getEgressBeaconListener(DataManager data) {
        for (Object o : DataUtils.getListenerModel(data)) {
            Map<String, String> definition = (Map<String, String>) o;
            String payload = DialogUtils.string(definition, "payload");
            if (!Listener.isEgressBeacon(payload)) continue;
            return (String) definition.get("name");
        }
        return null;
    }

    public static boolean isBeaconDefined(DataManager data) {
        return DataUtils.getEgressBeaconListener(data) != null;
    }

    public static LinkedList<Map<String, Object>> getListenerModel(DataManager data) {
        return new LinkedList(data.getMapSafe("listeners").values());
    }

    public static List getBeaconTranscriptAndSubscribe(DataManager data, String bid, Callback subs) {
        LinkedList result = data.getTranscriptAndSubscribeSafe("beaconlog", subs);
        Iterator i = result.iterator();
        while (i.hasNext()) {
            BeaconOutput next = (BeaconOutput) i.next();
            if (next.is(bid)) continue;
            i.remove();
        }
        return result;
    }

    public static List getScreenshotTranscript(DataManager data) {
        return data.getTranscriptSafe("screenshots");
    }

    public static List getKeystrokesTranscript(DataManager data) {
        return data.getTranscriptSafe("keystrokes");
    }

    public static BeaconCommands getBeaconCommands(DataManager data) {
        return (BeaconCommands) data.get("beacon_commands", null);
    }

    public static BeaconCommands getSSHCommands(DataManager data) {
        return (BeaconCommands) data.get("ssh_commands", null);
    }

    public static BeaconExploits getBeaconExploits(DataManager data) {
        return (BeaconExploits) data.get("beacon_exploits", null);
    }

    public static List getBeaconPowerShellCommands(DataManager data, String bid) {
        Map temp = data.getMapSafe("cmdlets");
        List res = (List) temp.get(bid);
        if (res == null) {
            return new LinkedList();
        }
        return res;
    }

    public static byte[] shellcode(GlobalDataManager data, String listenerName) {
        return DataUtils.shellcode(data, listenerName, false);
    }

    public static byte[] shellcodeX64(GlobalDataManager data, String listenerName) {
        return Stagers.shellcode(listenerName, "x64", false);
    }

    public static byte[] shellcode(GlobalDataManager data, String listenerName, boolean isRemoteSMB) {
        return Stagers.shellcode(listenerName, "x86", isRemoteSMB);
    }

    public static String getPrimaryStage(DataManager data) {
        LinkedList<Map<String, Object>> listeners = DataUtils.getListenerModel(data);
        for (Map<String, Object> listener : listeners) {
            String pl = (String) listener.get("payload");
            switch (pl) {
                case "windows/beacon_http/reverse_http":
                    return "HTTP Beacon";
                case "windows/beacon_https/reverse_https":
                    return "HTTPS Beacon";
                case "windows/beacon_dns/reverse_http":
                    return "DNS Beacon";
                case "windows/beacon_dns/reverse_dns_txt":
                    return "DNS Beacon";
            }
        }
        return "";
    }

    public static String getLocalIP(DataManager data) {
        return (String) data.get("localip", "127.0.0.1");
    }

    public static String getTeamServerIP(DataManager data) {
        return data.getMapSafe("options").get("host") + "";
    }

    public static List getInterfaceList(DataManager data) {
        LinkedList<Map> interfaces = data.getListSafe("interfaces");
        LinkedList results = interfaces.stream().map(next -> next.get("interface")).collect(Collectors.toCollection((Supplier<LinkedList>) LinkedList::new));
        return results;
    }

    public static Map getInterface(DataManager data, String intf) {
        LinkedList<Map> interfaces = data.getListSafe("interfaces");
        for (Map next : interfaces) {
            if (!intf.equals(next.get("interface"))) continue;
            return next;
        }
        return new HashMap();
    }

    public static String getManualProxySetting(DataManager data) {
        String temp = (String) data.getDataSafe("manproxy");
        if (temp == null) {
            return "";
        }
        return temp;
    }

    public static Map<String, Object> getGoldenTicket(DataManager data) {
        return data.getMapSafe("goldenticket");
    }

    public static String TokenToEmail(String token) {
        if (token == null || "".equals(token)) {
            return "unknown";
        }
        synchronized (tokenCache) {
            if (tokenCache.containsKey(token)) {
                return tokenCache.get(token);
            }
            GlobalDataManager data = GlobalDataManager.getGlobalDataManager();
            LinkedList<Map> tokens = data.getListSafe("tokens");
            for (Map next : tokens) {
                String ttoken = (String) next.get("token");
                String email = (String) next.get("email");
                tokenCache.put(ttoken, email);
            }
            if (tokenCache.containsKey(token)) {
                return tokenCache.get(token);
            }
        }
        return "unknown";
    }
}

