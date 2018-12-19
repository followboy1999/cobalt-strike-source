package beacon.pivots;

import aggressor.DataUtils;
import beacon.BeaconPivot;
import common.BeaconOutput;
import common.CommonUtils;
import dialog.DialogUtils;

public class SOCKSPivot
        extends BeaconPivot {
    @Override
    public void die() {
        this.client.getConnection().call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(this.bid, "socks stop")));
        this.client.getConnection().call("beacons.pivot_stop_port", CommonUtils.args(this.port + ""));
    }

    @Override
    public void tunnel() {
        String myip = DataUtils.getTeamServerIP(this.client.getData());
        DialogUtils.presentText("Tunnel via SOCKS", "Use this command in the Metasploit Framework to tunnel<br />exploits and auxiliary modules through this Beacon.<br />Use <strong>unsetg Proxies</strong> to stop tunneling through Beacon.", "setg Proxies socks4:" + myip + ":" + this.port);
    }
}

