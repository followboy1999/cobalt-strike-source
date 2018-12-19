package server;

import common.*;
import proxy.HTTPProxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BrowserPivotCalls implements ServerHook {
    protected Resources resources;
    protected Map sessions = new HashMap();

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("browserpivot.start", this);
        calls.put("browserpivot.stop", this);
    }

    public BrowserPivotCalls(Resources r) {
        this.resources = r;
    }

    public void start(Request r, ManageUser client) {
        final String bid = (String) r.arg(0);
        int myport = Integer.parseInt((String) r.arg(1));
        int fport = Integer.parseInt((String) r.arg(2));
        if (this.sessions.containsKey(bid)) {
            this.resources.broadcast("beaconlog", BeaconOutput.Error(bid, "This beacon already has a browser pivot session."));
            return;
        }
        try {
            HTTPProxy proxy = new HTTPProxy(myport, "127.0.0.1", fport);
            proxy.addProxyListener((type, text) -> {
                if (type == 0) {
                    BrowserPivotCalls.this.resources.broadcast("beaconlog", BeaconOutput.OutputB(bid, text));
                } else if (type == 1) {
                    BrowserPivotCalls.this.resources.broadcast("beaconlog", BeaconOutput.Error(bid, text));
                } else if (type == 2) {
                    BrowserPivotCalls.this.resources.broadcast("beaconlog", BeaconOutput.Output(bid, text));
                }
            });
            this.resources.broadcast("beaconlog", BeaconOutput.Output(bid, "Browser Pivot HTTP proxy is at: " + ServerUtils.getMyIP(this.resources) + ":" + myport));
            proxy.start();
            this.sessions.put(bid, proxy);
        } catch (IOException ioex) {
            this.resources.broadcast("beaconlog", BeaconOutput.Error(bid, "Could not start Browser Pivot on port " + myport + ": " + ioex.getMessage()));
            MudgeSanity.logException("browser pivot start", ioex, true);
        }
    }

    public void stop(Request r, ManageUser client) {
        String bid = (String) r.arg(0);
        HTTPProxy proxy = (HTTPProxy) this.sessions.get(bid);
        if (proxy != null) {
            proxy.stop();
            this.resources.call(client, r.derive("beacons.pivot_stop_port", CommonUtils.args(proxy.getPort())));
            this.sessions.remove(bid);
            this.resources.broadcast("beaconlog", BeaconOutput.OutputB(bid, "Stopped Browser Pivot"));
        } else {
            this.resources.broadcast("beaconlog", BeaconOutput.Error(bid, "There is no active browser pivot"));
        }
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("browserpivot.start", 3)) {
            this.start(r, client);
        } else if (r.is("browserpivot.stop", 1)) {
            this.stop(r, client);
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }

}

