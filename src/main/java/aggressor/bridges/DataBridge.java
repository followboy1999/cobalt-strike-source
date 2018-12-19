package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.*;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.io.File;
import java.util.*;

public class DataBridge implements Function,
        Loadable {
    protected Cortana engine;
    protected TeamQueue conn;
    protected AggressorClient client;

    public DataBridge(AggressorClient c, Cortana e, TeamQueue q) {
        this.client = c;
        this.engine = e;
        this.conn = q;
    }

    public static Map getKeys() {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("&applications", "applications");
        temp.put("&archives", "archives");
        temp.put("&credentials", "credentials");
        temp.put("&downloads", "downloads");
        temp.put("&keystrokes", "keystrokes");
        temp.put("&pivots", "socks");
        temp.put("&screenshots", "screenshots");
        temp.put("&services", "services");
        temp.put("&sites", "sites");
        temp.put("&targets", "targets");
        return temp;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&mynick", this);
        Cortana.put(si, "&tstamp", this);
        Cortana.put(si, "&dstamp", this);
        Cortana.put(si, "&tokenToEmail", this);
        Cortana.put(si, "&localip", this);
        Cortana.put(si, "&sync_download", this);
        Cortana.put(si, "&hosts", this);
        Cortana.put(si, "&host_update", this);
        Cortana.put(si, "&host_delete", this);
        Cortana.put(si, "&host_info", this);
        Cortana.put(si, "&credential_add", this);
        for (Object o : DataBridge.getKeys().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Cortana.put(si, (String) entry.getKey(), this);
        }
        Cortana.put(si, "&data_query", this);
        Cortana.put(si, "&data_keys", this);
        Cortana.put(si, "&resetData", this);
    }

    public static String getStringOrNull(Stack args) {
        if (args.isEmpty()) {
            return null;
        }
        Scalar value = (Scalar) args.pop();
        if (SleepUtils.isEmptyScalar(value)) {
            return null;
        }
        return value.toString();
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&mynick")) {
            return SleepUtils.getScalar(DataUtils.getNick(this.client.getData()));
        }
        if (name.equals("&localip")) {
            return SleepUtils.getScalar(DataUtils.getLocalIP(this.client.getData()));
        }
        if (name.equals("&dstamp")) {
            long when = BridgeUtilities.getLong(args);
            return SleepUtils.getScalar(CommonUtils.formatDate(when));
        }
        if (name.equals("&tstamp")) {
            long when = BridgeUtilities.getLong(args);
            return SleepUtils.getScalar(CommonUtils.formatTime(when));
        }
        if (name.equals("&tokenToEmail")) {
            String token = BridgeUtilities.getString(args, "");
            return SleepUtils.getScalar(DataUtils.TokenToEmail(token));
        }
        switch (name) {
            case "&host_delete": {
                String[] addr = ScriptUtils.ArrayOrString(args);
                for (String anAddr : addr) {
                    HashMap<String, String> temp = new HashMap<>();
                    temp.put("address", anAddr);
                    String key = CommonUtils.TargetKey(temp);
                    this.client.getConnection().call("targets.remove", CommonUtils.args(key));
                }
                this.client.getConnection().call("targets.push");
                break;
            }
            case "&host_update": {
                String[] addr = ScriptUtils.ArrayOrString(args);
                String tname = DataBridge.getStringOrNull(args);
                String os = DataBridge.getStringOrNull(args);
                double osver = BridgeUtilities.getDouble(args, 0.0);
                String note = DataBridge.getStringOrNull(args);
                for (String anAddr : addr) {
                    HashMap<String, String> temp = new HashMap<>();
                    temp.put("address", CommonUtils.trim(anAddr));
                    if (tname != null) {
                        temp.put("name", tname);
                    }
                    if (note != null) {
                        temp.put("note", note);
                    }
                    if (os != null) {
                        temp.put("os", os);
                    }
                    if (osver != 0.0) {
                        temp.put("version", osver + "");
                    }
                    String key = CommonUtils.TargetKey(temp);
                    this.client.getConnection().call("targets.update", CommonUtils.args(key, temp));
                }
                this.client.getConnection().call("targets.push");
                break;
            }
            default:
                if ("&host_info".equals(name)) {
                    String addr = BridgeUtilities.getString(args, "");
                    Map entry = this.client.getData().getModelDirect("targets", addr);
                    return ScriptUtils.IndexOrMap(entry, args);
                }
                if ("&hosts".equals(name)) {
                    List targets = (List) this.client.getData().getDataSafe("targets");
                    LinkedList results = new LinkedList();
                    for (Object target : targets) {
                        Map entry = (Map) target;
                        results.add(entry.get("address"));
                    }
                    return ScriptUtils.convertAll(results);
                }
                if ("&data_keys".equals(name)) {
                    return ScriptUtils.convertAll(this.client.getData().getDataKeys());
                }
                if ("&data_query".equals(name)) {
                    String key = BridgeUtilities.getString(args, "");
                    return ScriptUtils.convertAll(this.client.getData().getDataSafe(key));
                }
                if ("&resetData".equals(name)) {
                    this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Notify(DataUtils.getNick(this.client.getData()) + " reset the data model.")), null);
                    this.client.getConnection().call("aggressor.reset_data");
                } else if ("&credential_add".equals(name)) {
                    String user = BridgeUtilities.getString(args, "");
                    String pass = BridgeUtilities.getString(args, "");
                    String realm = BridgeUtilities.getString(args, "");
                    String source = BridgeUtilities.getString(args, "manual");
                    String host = BridgeUtilities.getString(args, "");
                    HashMap<String, String> temp = new HashMap<>();
                    temp.put("user", user);
                    temp.put("password", pass);
                    temp.put("realm", realm);
                    temp.put("source", source);
                    temp.put("host", host);
                    this.client.getConnection().call("credentials.add", CommonUtils.args(CommonUtils.CredKey(temp), temp));
                    this.client.getConnection().call("credentials.push");
                } else if ("&sync_download".equals(name)) {
                    String rpath = BridgeUtilities.getString(args, "");
                    String ldest = BridgeUtilities.getString(args, "");
                    DownloadNotify notify = null;
                    if (!args.isEmpty()) {
                        Scalar function2 = (Scalar) args.pop();
                        notify = (DownloadNotify) ObjectUtilities.buildArgument(DownloadNotify.class, function2, script);
                    }
                    new DownloadFileSimple(this.client.getConnection(), rpath, new File(ldest), notify).start();
                } else {
                    Map funcs = DataBridge.getKeys();
                    if (funcs.containsKey(name)) {
                        String key = (String) funcs.get(name);
                        return ScriptUtils.convertAll(this.client.getData().getDataSafe(key));
                    }
                }
                break;
        }
        return SleepUtils.getEmptyScalar();
    }
}

