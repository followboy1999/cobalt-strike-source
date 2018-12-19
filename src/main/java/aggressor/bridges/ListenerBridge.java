package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.Listener;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ListenerBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public ListenerBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&listener_create", this);
        Cortana.put(si, "&listener_delete", this);
        Cortana.put(si, "&listener_restart", this);
        Cortana.put(si, "&listeners", this);
        Cortana.put(si, "&listeners_local", this);
        Cortana.put(si, "&listener_info", this);
        Cortana.put(si, "&listener_describe", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&listener_create".equals(name)) {
            String lname = BridgeUtilities.getString(args, "");
            String payload = BridgeUtilities.getString(args, "");
            String host = BridgeUtilities.getString(args, "");
            int port = BridgeUtilities.getInt(args, 80);
            String beacons = BridgeUtilities.getString(args, "");
            HashMap<String, Object> options = new HashMap<>();
            options.put("name", lname);
            options.put("payload", payload);
            options.put("host", host);
            options.put("port", port);
            options.put("beacons", beacons);
            this.client.getConnection().call("listeners.stop", CommonUtils.args(lname));
            this.client.getConnection().call("listeners.create", CommonUtils.args(lname, options));
        } else if ("&listener_delete".equals(name)) {
            String lname = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("listeners.remove", CommonUtils.args(lname));
        } else if ("&listener_restart".equals(name)) {
            String lname = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("listeners.restart", CommonUtils.args(lname));
        } else {
            if ("&listeners".equals(name)) {
                List listeners = Listener.getListenerNamesWithSMB(this.client.getData());
                return SleepUtils.getArrayWrapper(listeners);
            }
            if ("&listeners_local".equals(name)) {
                List listeners = Listener.getListenerNamesWithSMBLocal(this.client.getData());
                return SleepUtils.getArrayWrapper(listeners);
            }
            if ("&listener_info".equals(name)) {
                try {
                    String lname = BridgeUtilities.getString(args, "");
                    Listener listener = Listener.getListener(lname);
                    if (args.isEmpty()) {
                        return SleepUtils.getHashWrapper(listener.toMap());
                    }
                    String key = BridgeUtilities.getString(args, "");
                    return SleepUtils.getScalar(listener.toMap().get(key) + "");
                } catch (RuntimeException rex) {
                    return SleepUtils.getEmptyScalar();
                }
            }
            if ("&listener_describe".equals(name)) {
                String lname = BridgeUtilities.getString(args, "");
                Listener listener = Listener.getListener(lname);
                if (args.isEmpty()) {
                    return SleepUtils.getScalar(listener.toString());
                }
                String target = BridgeUtilities.getString(args, "");
                return SleepUtils.getScalar(listener.toString(target));
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}

