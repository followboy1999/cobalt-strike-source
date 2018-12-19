package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import cortana.Cortana;
import dialog.DialogUtils;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Map;
import java.util.Stack;

public class CovertVPNBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public CovertVPNBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&vpn_tap_create", this);
        Cortana.put(si, "&vpn_tap_delete", this);
        Cortana.put(si, "&vpn_interfaces", this);
        Cortana.put(si, "&vpn_interface_info", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&vpn_tap_create".equals(name)) {
            String intf = BridgeUtilities.getString(args, "");
            String hwaddr = BridgeUtilities.getString(args, "");
            String host = BridgeUtilities.getString(args, "");
            String port = BridgeUtilities.getInt(args, 0) + "";
            String channel = BridgeUtilities.getString(args, "udp");
            if ("".equals(hwaddr)) {
                hwaddr = CommonUtils.randomMac();
            }
            if ("udp".equals(channel)) {
                channel = "UDP";
            } else if ("http".equals(channel)) {
                channel = "HTTP";
            } else if ("icmp".equals(channel)) {
                channel = "ICMP";
            } else if ("bind".equals(channel)) {
                channel = "TCP (Bind)";
            } else if ("reverse".equals(channel)) {
                channel = "TCP (Reverse)";
            } else {
                throw new RuntimeException("Unknown channel: '" + channel + "'");
            }
            this.client.getConnection().call("cloudstrike.start_tap", CommonUtils.args(intf, hwaddr, port, channel));
        } else if ("&vpn_tap_delete".equals(name)) {
            String intf = BridgeUtilities.getString(args, "");
            Map vals = DataUtils.getInterface(this.client.getData(), intf);
            String channel = DialogUtils.string(vals, "channel");
            String port = DialogUtils.string(vals, "port");
            if ("TCP (Bind)".equals(channel)) {
                this.client.getConnection().call("beacons.pivot_stop_port", CommonUtils.args(port));
            }
            this.client.getConnection().call("cloudstrike.stop_tap", CommonUtils.args(intf));
        } else {
            if ("&vpn_interfaces".equals(name)) {
                return SleepUtils.getArrayWrapper(DataUtils.getInterfaceList(this.client.getData()));
            }
            if ("&vpn_interface_info".equals(name)) {
                String intf = BridgeUtilities.getString(args, "");
                Map vals = DataUtils.getInterface(this.client.getData(), intf);
                if (args.isEmpty()) {
                    return SleepUtils.getHashWrapper(vals);
                }
                String key = BridgeUtilities.getString(args, "");
                return CommonUtils.convertAll(vals.get(key));
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}

