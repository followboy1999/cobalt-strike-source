package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class SiteBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public SiteBridge(AggressorClient c) {
        this.client = c;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&site_kill", this);
        Cortana.put(si, "&site_host", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&site_kill".equals(name)) {
            String port = BridgeUtilities.getString(args, "");
            String URI2 = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("cloudstrike.kill_site", CommonUtils.args(port, URI2));
        } else if ("&site_host".equals(name)) {
            String host = BridgeUtilities.getString(args, "");
            int port = BridgeUtilities.getInt(args, 80);
            String uri = BridgeUtilities.getString(args, "");
            String content = BridgeUtilities.getString(args, "");
            String mimet = BridgeUtilities.getString(args, "application/octet-stream");
            String desc = BridgeUtilities.getString(args, "content");
            boolean ssl = !args.isEmpty() && SleepUtils.isTrueScalar((Scalar) args.pop());
            String proto = ssl ? "https://" : "http://";
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(host, port, ssl, uri, content, mimet, desc));
            return SleepUtils.getScalar(proto + host + ":" + port + uri);
        }
        return SleepUtils.getEmptyScalar();
    }
}

