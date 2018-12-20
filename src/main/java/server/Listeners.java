package server;

import common.CommonUtils;
import common.ListenerConfig;
import common.Reply;
import common.Request;

import java.util.HashMap;
import java.util.Map;

public class Listeners implements ServerHook {
    protected Resources resources;
    protected PersistentData store;
    protected Map<String, Map> listeners;
    protected ListenerConfig config;

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("listeners.create", this);
        calls.put("listeners.remove", this);
        calls.put("listeners.go", this);
        calls.put("listeners.restart", this);
        calls.put("listeners.stop", this);
        calls.put("listeners.localip", this);
        calls.put("listeners.set_status", this);
        calls.put("listeners.export", this);
    }

    public Listeners(Resources r) {
        this.listeners = new HashMap<>();
        this.config = null;
        this.resources = r;
        this.config = new ListenerConfig(ServerUtils.getProfile(r));
        this.store = new PersistentData("listeners", this);
        this.listeners = (Map) this.store.getValue(new HashMap());
        this.resources.broadcast("listeners", this.buildListenerModel(), true);
        this.resources.broadcast("localip", ServerUtils.getMyIP(this.resources), true);
    }

    public void save() {
        this.store.save(this.listeners);
    }

    public HashMap<String, HashMap> buildListenerModel() {
        synchronized (this) {
            HashMap<String, HashMap> result = new HashMap<>();
            for (Map.Entry<String, Map> entry : this.listeners.entrySet()) {
                String key = entry.getKey();
                HashMap value = new HashMap(entry.getValue());
                value.put("config", this.config);
                result.put(key, value);
            }
            return result;
        }
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("listeners.create", 2)) {
            String name = r.arg(0) + "";
            Map data = (Map) r.arg(1);
            synchronized (this) {
                this.listeners.put(name, data);
                this.save();
                this.resources.broadcast("listeners", this.buildListenerModel(), true);
            }
            if (isBeacon(data)) {
                this.resources.call(client, r.derive("beacons.start", CommonUtils.args(data)));
            }
        } else if (r.is("listeners.remove", 1)) {
            String name = r.arg(0) + "";
            Map entry;
            synchronized (this) {
                entry = this.listeners.get(name);
                if (entry == null) {
                    return;
                }
                this.listeners.remove(name);
                this.save();
                this.resources.broadcast("listeners", this.buildListenerModel(), true);
            }
            if (isBeacon(entry)) {
                this.resources.call(client, r.derive("beacons.stop", CommonUtils.args(entry)));
            }
        } else if (r.is("listeners.restart", 1)) {
            String name = r.arg(0) + "";
            Map entry;
            synchronized (this) {
                entry = this.listeners.get(name);
            }
            if (isBeacon(entry)) {
                this.resources.call("beacons.stop", CommonUtils.args(entry));
                this.resources.call(client, r.derive("beacons.start", CommonUtils.args(entry)));
            }
        } else if (r.is("listeners.stop", 1)) {
            String name = r.arg(0) + "";
            Map entry;
            synchronized (this) {
                entry = this.listeners.get(name);
            }
            if (entry == null) {
                return;
            }
            if (isBeacon(entry)) {
                this.resources.call("beacons.stop", CommonUtils.args(entry));
            }
        } else if (r.is("listeners.go", 0)) {
            for (Map.Entry<String, HashMap> next : this.buildListenerModel().entrySet()) {
                if (isBeacon(next.getValue())) {
                    this.resources.call("beacons.start", CommonUtils.args(next.getValue()));
                }
            }
        } else if (r.is("listeners.localip", 1)) {
            String myip = r.arg(0) + "";
            this.resources.put("localip", myip);
            this.resources.broadcast("localip", myip, true);
        } else if (r.is("listeners.set_status", 2)) {
            String name = r.arg(0) + "";
            String status = r.arg(1) + "";
            Map entry2;
            synchronized (this) {
                entry2 = this.listeners.get(name);
                entry2.put("status", status);
                this.resources.broadcast("listeners", this.buildListenerModel(), true);
            }
        } else if (r.is("listeners.export", 3)) {
            String listenerName = (String) r.arg(0);
            String arch = (String) r.arg(1);
            String proxyconfig = (String) r.arg(2);
            Map entry3;
            synchronized (this) {
                entry3 = this.listeners.get(listenerName);
            }
            if (entry3 == null) {
                CommonUtils.print_error("Listener '" + listenerName + "' does not exist (listeners.export)");
            } else {
                this.resources.call(client, r.derive("beacons.export_stage_generic", CommonUtils.args(entry3, arch, proxyconfig)));
            }
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }

    public static boolean isBeacon(Map listener) {
        return (listener.get("payload") + "").contains("beacon") && !"windows/beacon_smb/bind_pipe".equals(listener.get("payload"));
    }
}
