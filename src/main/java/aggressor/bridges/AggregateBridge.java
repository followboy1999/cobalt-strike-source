package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.Keys;
import common.RegexParser;
import cortana.Cortana;
import data.DataAggregate;
import dialog.DialogUtils;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScalarHash;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class AggregateBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public AggregateBridge(AggressorClient c) {
        this.client = c;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&aggregate", this);
        Cortana.put(si, "&agConvert", this);
        Cortana.put(si, "&agArchives", this);
        Iterator i = Keys.getDataModelIterator();
        while (i.hasNext()) {
            String model = (String) i.next();
            model = model.substring(0, 1).toUpperCase() + model.substring(1);
            final String keyMajor = model.toLowerCase();
            Cortana.put(si, "&ag" + model, (name, script, args) -> {
                Map temp = (Map) BridgeUtilities.getObject(args);
                return CommonUtils.convertAll(temp.get(keyMajor));
            });
        }
        Cortana.put(si, "&agServicesForHost", this);
        Cortana.put(si, "&agSessionsForHost", this);
        Cortana.put(si, "&agCredentialsForHost", this);
        Cortana.put(si, "&agWebHitsForEmail", this);
        Cortana.put(si, "&agWebHitsForToken", this);
        Cortana.put(si, "&agCountWebHitsByToken", this);
        Cortana.put(si, "&agSentEmailsForCampaign", this);
        Cortana.put(si, "&agSentEmailsForEmailAddress", this);
        Cortana.put(si, "&agApplicationsForEmailAddress", this);
        Cortana.put(si, "&agFileIndicatorsForSession", this);
        Cortana.put(si, "&agFileIndicators", this);
        Cortana.put(si, "&agOtherIndicatorsForSession", this);
        Cortana.put(si, "&agTasksAndCheckinsForSession", this);
        Cortana.put(si, "&agServices", this);
        Cortana.put(si, "&agArchivesByTactic", this);
        Cortana.put(si, "&agTokenToEmail", this);
        Cortana.put(si, "&agEmailAddresses", this);
        Cortana.put(si, "&agCampaigns", this);
        Cortana.put(si, "&agSentEmails", this);
        Cortana.put(si, "&agIndicators", this);
        Cortana.put(si, "&agInputs", this);
        Cortana.put(si, "&agInputsForSession", this);
        Cortana.put(si, "&agTasks", this);
        Cortana.put(si, "&agWebHits", this);
        Cortana.put(si, "&agWebHitsWithTokens", this);
        Cortana.put(si, "&agSessionsById", this);
        Cortana.put(si, "&agC2Domains", this);
        Cortana.put(si, "&agC2ForSample", this);
        Cortana.put(si, "&agPEForSample", this);
        Cortana.put(si, "&agPENotesForSample", this);
        Cortana.put(si, "&agCommunicationPathForSession", this);
        Cortana.put(si, "&agC2Samples", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    public Scalar extractValue(Map values, String keyMajor) {
        List temp = (List) values.get(keyMajor);
        return CommonUtils.convertAll(temp);
    }

    public static List filterList(List major, String minorKey, String minorValue) {
        LinkedList<Map> results = new LinkedList<>();
        for (Object aMajor : major) {
            Map next = (Map) aMajor;
            String value;
            if (!next.containsKey(minorKey) || !minorValue.equals(value = next.get(minorKey).toString())) continue;
            results.add(next);
        }
        return results;
    }

    public static List filterListBySetMember(List major, String minorKey, String minorValue) {
        LinkedList<Map> results = new LinkedList<>();
        for (Object aMajor : major) {
            Map next = (Map) aMajor;
            Set value;
            if (!next.containsKey(minorKey) || !(value = CommonUtils.toSet(next.get(minorKey).toString())).contains(minorValue))
                continue;
            results.add(next);
        }
        return results;
    }

    public static List filterList(List major, String minorKey, Set wanted) {
        LinkedList<Map> results = new LinkedList<>();
        for (Object aMajor : major) {
            Map next = (Map) aMajor;
            String value;
            if (!next.containsKey(minorKey) || !wanted.contains(value = next.get(minorKey).toString())) continue;
            results.add(next);
        }
        return results;
    }

    public static List filterListNot(List major, String minorKey, String minorValue) {
        LinkedList<Map> results = new LinkedList<>();
        for (Object aMajor : major) {
            Map next = (Map) aMajor;
            String value;
            if (!next.containsKey(minorKey) || minorValue.equals(value = next.get(minorKey).toString())) continue;
            results.add(next);
        }
        return results;
    }

    public static List getValuesWithout(List entries, String key) {
        LinkedHashSet results = new LinkedHashSet();
        for (Object entry : entries) {
            HashMap next = new HashMap((Map) entry);
            next.remove(key);
            results.add(next);
        }
        return new LinkedList(results);
    }

    public static List getValue(List value, String key) {
        LinkedList results = new LinkedList();
        for (Object aValue : value) {
            Map next = (Map) aValue;
            results.add(next.get(key));
        }
        return results;
    }

    public static List join(List a, List b, String key) {
        return AggregateBridge.join(a, b, key, key);
    }

    public static List join(List a, List b, String keyA, String keyB) {
        LinkedList results = new LinkedList();
        HashMap<String, Map> byvalue = new HashMap<>();
        Iterator i = a.iterator();
        while (i.hasNext()) {
            Map next = (Map) i.next();
            String val = next.get(keyA) + "";
            byvalue.put(val, next);
        }
        i = b.iterator();
        while (i.hasNext()) {
            Map next = (Map) i.next();
            String val = next.get(keyB) + "";
            HashMap temp = new HashMap();
            temp.putAll(next);
            if (byvalue.containsKey(val)) {
                temp.putAll(byvalue.get(val));
            }
            results.add(temp);
        }
        return results;
    }

    public static Map toMap(List model, String key) {
        HashMap<String, Map> results = new HashMap<>();
        for (Object aModel : model) {
            Map next = (Map) aModel;
            String bidz = (String) next.get(key);
            results.put(bidz, next);
        }
        return results;
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&aggregate".equals(name)) {
            return SleepUtils.getScalar(DataAggregate.AllModels(this.client));
        }
        if ("&agArchives".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            return CommonUtils.convertAll(temp.get("archives"));
        }
        if ("&agArchivesByTactic".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String tactic = BridgeUtilities.getString(args, "");
            List archives = AggregateBridge.filterListBySetMember((List) temp.get("archives"), "tactic", tactic);
            return CommonUtils.convertAll(archives);
        }
        if ("&agConvert".equals(name)) {
            Object temp = BridgeUtilities.getObject(args);
            return CommonUtils.convertAll(temp);
        }
        if ("&agTokenToEmail".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String token = BridgeUtilities.getString(args, "");
            List tokens = (List) temp.get("tokens");
            for (Object token1 : tokens) {
                Map next = (Map) token1;
                if (!token.equals(next.get("token"))) continue;
                return SleepUtils.getScalar((String) next.get("email"));
            }
            return SleepUtils.getEmptyScalar();
        }
        if ("&agCampaigns".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List archives = (List) temp.get("archives");
            HashMap<String, Map> results = new HashMap<>();
            HashMap count = new HashMap();
            for (Object archive : archives) {
                Map next = (Map) archive;
                String cid;
                if ("sendmail_start".equals(next.get("type"))) {
                    cid = next.get("cid").toString();
                    results.put(cid, next);
                    continue;
                }
                if (!"sendmail_post".equals(next.get("type"))) continue;
                cid = DialogUtils.string(next, "cid");
                String status = DialogUtils.string(next, "status");
                if (!"SUCCESS".equals(status)) continue;
                CommonUtils.increment(count, cid);
            }
            Iterator j = results.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry next = (Map.Entry) j.next();
                String cid = (String) next.getKey();
                if (CommonUtils.count(count, cid) != 0) continue;
                j.remove();
            }
            return CommonUtils.convertAll(results);
        }
        if ("&agC2Samples".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List values = (List) temp.get("c2samples");
            return CommonUtils.convertAll(values);
        }
        if ("&agC2Domains".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List entries = AggregateBridge.getValue((List) temp.get("c2info"), "domains");
            HashSet results = new HashSet();
            for (Object entry : entries) {
                String domains = (String) entry;
                results.addAll(CommonUtils.toSet(domains));
            }
            LinkedList done = new LinkedList(results);
            Collections.sort(done);
            return CommonUtils.convertAll(done);
        }
        if ("&agPEForSample".equals(name)) {
            Map temp = SleepUtils.getMapFromHash((ScalarHash) BridgeUtilities.getObject(args));
            Map entries = (Map) temp.get("pe");
            LinkedHashMap<String, String> result = new LinkedHashMap<>();
            result.put("Checksum", entries.get("Checksum") + "");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            Date mydate = (Date) entries.get("Compilation Timestamp");
            result.put("Compilation Timestamp", dateFormat.format(mydate));
            result.put("Entry Point", entries.get("Entry Point") + "");
            if (entries.containsKey("Name")) {
                result.put("Name", (String) entries.get("Name"));
            }
            long sz = CommonUtils.toLongNumber(entries.get("Size") + "", 0L);
            String size = CommonUtils.formatSize(sz) + " (" + sz + " bytes)";
            result.put("Size", size);
            result.put("Target Machine", (String) entries.get("Target Machine"));
            Scalar hash2 = SleepUtils.getOrderedHashScalar();
            for (Object o : result.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                Scalar key = SleepUtils.getScalar((String) entry.getKey());
                Scalar value = hash2.getHash().getAt(key);
                value.setValue(SleepUtils.getScalar(entry.getValue() + ""));
            }
            return hash2;
        }
        if ("&agPENotesForSample".equals(name)) {
            Map temp = SleepUtils.getMapFromHash((ScalarHash) BridgeUtilities.getObject(args));
            Map entries = (Map) temp.get("pe");
            LinkedHashMap result = new LinkedHashMap();
            if (entries.containsKey("Notes")) {
                return SleepUtils.getScalar((String) entries.get("Notes"));
            }
            return SleepUtils.getEmptyScalar();
        }
        if ("&agC2ForSample".equals(name)) {
            Map temp = SleepUtils.getMapFromHash((ScalarHash) BridgeUtilities.getObject(args));
            List entries = AggregateBridge.getValuesWithout((List) temp.get("callbacks"), "bid");
            LinkedList results = new LinkedList();
            for (Object entry1 : entries) {
                Map entry = (Map) entry1;
                Set domains = CommonUtils.toSet((String) entry.get("domains"));
                boolean isSSL = "1".equals(entry.get("ssl").toString());
                boolean isDNS = "1".equals(entry.get("dns").toString());
                String protos = isSSL ? "HTTPS" : (isDNS ? "DNS, HTTP" : "HTTP");
                for (Object domain1 : domains) {
                    String domain = (String) domain1;
                    HashMap<String, String> next = new HashMap<>();
                    next.put("Host", domain);
                    next.put("Port", (String) entry.get("port"));
                    next.put("Protocols", protos);
                    results.add(next);
                }
            }
            return CommonUtils.convertAll(results);
        }
        if ("&agCommunicationPathForSession".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String bid = BridgeUtilities.getString(args, "");
            CommonUtils.print_info("Path for: " + bid);
            Map sessions = AggregateBridge.toMap((List) temp.get("sessions"), "id");
            Map c2info = AggregateBridge.toMap((List) temp.get("c2info"), "bid");
            LinkedList paths = new LinkedList();
            Map current = (Map) sessions.get(bid);
            while (current != null) {
                if (!"".equals(current.get("pbid"))) {
                    HashMap<String, String> tempz = new HashMap<>();
                    if ("beacon".equals(current.get("session"))) {
                        tempz.put("protocol", "SMB");
                        tempz.put("port", "445");
                    } else {
                        tempz.put("protocol", "SSH");
                        tempz.put("port", (String) current.get("port"));
                    }
                    bid = (String) current.get("pbid");
                    current = (Map) sessions.get(bid);
                    if (current == null) continue;
                    current.remove("port");
                    current.remove("protocol");
                    tempz.put("hosts", (String) current.get("computer"));
                    tempz.putAll(current);
                    paths.add(tempz);
                    continue;
                }
                current = null;
                Map info = (Map) c2info.get(bid);
                if (info == null) continue;
                String domains = DialogUtils.string(info, "domains");
                boolean dns = DialogUtils.bool(info, "dns");
                boolean ssl = DialogUtils.bool(info, "ssl");
                int port = DialogUtils.number(info, "port");
                HashMap<String, String> tempz = new HashMap<>();
                if (dns) {
                    tempz.put("protocol", "DNS, HTTP");
                    tempz.put("port", "53, " + port);
                } else if (ssl) {
                    tempz.put("protocol", "HTTPS");
                    tempz.put("port", "" + port);
                } else {
                    tempz.put("protocol", "HTTP");
                    tempz.put("port", "" + port);
                }
                tempz.put("hosts", domains);
                paths.add(tempz);
            }
            CommonUtils.print_info("\t" + paths);
            return CommonUtils.convertAll(paths);
        }
        if ("&agSessionsById".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List archives = (List) temp.get("sessions");
            Map sessions = AggregateBridge.toMap((List) temp.get("sessions"), "id");
            return CommonUtils.convertAll(sessions);
        }
        if ("&agEmailAddresses".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            LinkedList emails = new LinkedList(new HashSet(AggregateBridge.getValue((List) temp.get("tokens"), "email")));
            return CommonUtils.convertAll(emails);
        }
        if ("&agServicesForHost".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String host = BridgeUtilities.getString(args, "");
            List services = AggregateBridge.filterList((List) temp.get("services"), "address", host);
            return CommonUtils.convertAll(services);
        }
        if ("&agServices".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            LinkedList services = new LinkedList((List) temp.get("services"));
            HashSet targets = new HashSet(AggregateBridge.getValue((List) temp.get("targets"), "address"));
            Iterator i = services.iterator();
            while (i.hasNext()) {
                Map next = (Map) i.next();
                String host = (String) next.get("address");
                if (targets.contains(host)) continue;
                i.remove();
            }
            return CommonUtils.convertAll(services);
        }
        if ("&agSessionsForHost".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String host = BridgeUtilities.getString(args, "");
            List sessions = AggregateBridge.filterList((List) temp.get("sessions"), "internal", host);
            return CommonUtils.convertAll(sessions);
        }
        if ("&agCredentialsForHost".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String nameh = BridgeUtilities.getString(args, "");
            List creds = AggregateBridge.filterList((List) temp.get("credentials"), "host", nameh);
            return CommonUtils.convertAll(creds);
        }
        if ("&agWebHitsForToken".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String token = BridgeUtilities.getString(args, "");
            List webhits = AggregateBridge.filterList((List) temp.get("archives"), "type", "webhit");
            List results = AggregateBridge.filterList(webhits, "token", token);
            return CommonUtils.convertAll(results);
        }
        if ("&agSentEmailsForCampaign".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String cid = BridgeUtilities.getString(args, "");
            List emails = AggregateBridge.filterList((List) temp.get("archives"), "type", "sendmail_post");
            List results = AggregateBridge.filterList(emails, "cid", cid);
            List tokens = (List) temp.get("tokens");
            return CommonUtils.convertAll(AggregateBridge.join(tokens, results, "token"));
        }
        if ("&agSentEmailsForEmailAddress".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String email = BridgeUtilities.getString(args, "");
            List emails = AggregateBridge.filterList((List) temp.get("archives"), "type", "sendmail_post");
            List emailst = AggregateBridge.join((List) temp.get("tokens"), emails, "token");
            List results = AggregateBridge.filterList(emailst, "email", email);
            return CommonUtils.convertAll(results);
        }
        if ("&agApplicationsForEmailAddress".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String email = BridgeUtilities.getString(args, "");
            List apps = (List) temp.get("applications");
            List appst = AggregateBridge.join((List) temp.get("tokens"), apps, "token", "id");
            List results = AggregateBridge.filterList(appst, "email", email);
            return CommonUtils.convertAll(results);
        }
        if ("&agCountWebHitsByToken".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            HashMap<String, Integer> results = new HashMap<>();
            List webhits = AggregateBridge.filterList((List) temp.get("archives"), "type", "webhit");
            List tokens = AggregateBridge.getValue((List) temp.get("tokens"), "token");
            for (Object token1 : tokens) {
                String token = (String) token1;
                int size = AggregateBridge.filterList(webhits, "token", token).size();
                if (size <= 0) continue;
                results.put(token, size);
            }
            return CommonUtils.convertAll(results);
        }
        if ("&agWebHitsForEmail".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String email = BridgeUtilities.getString(args, "");
            LinkedList results = new LinkedList();
            List webhits = AggregateBridge.filterList((List) temp.get("archives"), "type", "webhit");
            List tokens = AggregateBridge.getValue(AggregateBridge.filterList((List) temp.get("tokens"), "email", email), "token");
            for (Object token1 : tokens) {
                String token = (String) token1;
                results.addAll(AggregateBridge.filterList(webhits, "token", token));
            }
            return CommonUtils.convertAll(results);
        }
        if ("&agSentEmails".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List results = AggregateBridge.filterList((List) temp.get("archives"), "type", "sendmail_post");
            return CommonUtils.convertAll(results);
        }
        if ("&agIndicators".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List results = AggregateBridge.filterList((List) temp.get("archives"), "type", "indicator");
            return CommonUtils.convertAll(results);
        }
        if ("&agFileIndicators".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String bid = BridgeUtilities.getString(args, "");
            List indicators = AggregateBridge.filterList((List) temp.get("archives"), "type", "indicator");
            HashSet<String> seen = new HashSet<>();
            Iterator i = indicators.iterator();
            while (i.hasNext()) {
                Map next = (Map) i.next();
                String data = (String) next.get("data");
                RegexParser parser = new RegexParser(data);
                if (parser.matches("file: (.*?) (.*?) bytes (.*)")) {
                    String hash3 = parser.group(1);
                    String size = parser.group(2);
                    String namef = parser.group(3);
                    if (seen.contains(hash3)) {
                        i.remove();
                        continue;
                    }
                    next.put("hash", hash3);
                    next.put("name", namef);
                    next.put("size", size);
                    seen.add(hash3);
                    continue;
                }
                i.remove();
            }
            return CommonUtils.convertAll(indicators);
        }
        if ("&agFileIndicatorsForSession".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String bid = BridgeUtilities.getString(args, "");
            List indicators = AggregateBridge.filterList((List) temp.get("archives"), "type", "indicator");
            List tempr = AggregateBridge.filterList(indicators, "bid", bid);
            LinkedList results = new LinkedList();
            for (Object aTempr : tempr) {
                HashMap<String, String> next = new HashMap<String, String>((Map) aTempr);
                String data = next.get("data");
                RegexParser parser = new RegexParser(data);
                if (!parser.matches("file: (.*?) (.*?) bytes (.*)")) continue;
                String hash4 = parser.group(1);
                String size = parser.group(2);
                String namef = parser.group(3);
                next.put("hash", hash4);
                next.put("name", namef);
                next.put("size", size);
                results.add(next);
            }
            return CommonUtils.convertAll(results);
        }
        if ("&agOtherIndicatorsForSession".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String bid = BridgeUtilities.getString(args, "");
            List indicators = AggregateBridge.filterList((List) temp.get("archives"), "type", "indicator");
            List tempr = AggregateBridge.filterList(indicators, "bid", bid);
            LinkedList results = new LinkedList();
            for (Object aTempr : tempr) {
                HashMap<String, String> next = new HashMap<String, String>((Map) aTempr);
                String data = next.get("data");
                RegexParser parser = new RegexParser(data);
                if (!parser.matches("service: (.*?) (.*)")) continue;
                String target = parser.group(1);
                String namei = parser.group(2);
                next.put("target", target);
                next.put("name", namei);
                next.put("type", "service");
                results.add(next);
            }
            return CommonUtils.convertAll(results);
        }
        if ("&agTasksAndCheckinsForSession".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String bid = BridgeUtilities.getString(args, "");
            Set wanted = CommonUtils.toSet("task, checkin, output");
            List indicators = AggregateBridge.filterList((List) temp.get("archives"), "type", wanted);
            List results = AggregateBridge.filterList(indicators, "bid", bid);
            return CommonUtils.convertAll(results);
        }
        if ("&agInputs".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List results = AggregateBridge.filterList((List) temp.get("archives"), "type", "input");
            return CommonUtils.convertAll(results);
        }
        if ("&agInputsForSession".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            String bid = BridgeUtilities.getString(args, "");
            List indicators = AggregateBridge.filterList((List) temp.get("archives"), "type", "input");
            List results = AggregateBridge.filterList(indicators, "bid", bid);
            return CommonUtils.convertAll(results);
        }
        if ("&agTasks".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List results = AggregateBridge.filterList((List) temp.get("archives"), "type", "task");
            return CommonUtils.convertAll(results);
        }
        if ("&agWebHits".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List results = AggregateBridge.filterList((List) temp.get("archives"), "type", "webhit");
            return CommonUtils.convertAll(results);
        }
        if ("&agWebHitsWithTokens".equals(name)) {
            Map temp = (Map) BridgeUtilities.getObject(args);
            List results = AggregateBridge.filterList((List) temp.get("archives"), "type", "webhit");
            results = AggregateBridge.filterListNot(results, "token", "");
            return CommonUtils.convertAll(results);
        }
        return SleepUtils.getEmptyScalar();
    }

}

