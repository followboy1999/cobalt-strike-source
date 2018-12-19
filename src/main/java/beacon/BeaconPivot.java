package beacon;

import aggressor.AggressorClient;
import beacon.pivots.PortForwardPivot;
import beacon.pivots.ReversePortForwardPivot;
import beacon.pivots.SOCKSPivot;
import dialog.DialogUtils;

import java.util.Arrays;
import java.util.Map;

public abstract class BeaconPivot {
    protected AggressorClient client = null;
    protected String bid = null;
    protected int port = 0;

    public static BeaconPivot resolve(AggressorClient client, Map entry) {
        BeaconPivot rv;
        String type = DialogUtils.string(entry, "type");
        String bid = DialogUtils.string(entry, "bid");
        int port = DialogUtils.number(entry, "port");
        rv = type.equals("reverse port forward") ? new ReversePortForwardPivot() : (type.equals("port forward") ? new PortForwardPivot() : new SOCKSPivot());
        rv.client = client;
        rv.bid = bid;
        rv.port = port;
        return rv;
    }

    public static BeaconPivot[] resolve(AggressorClient client, Map[] entries) {
        BeaconPivot[] rv = Arrays.stream(entries).map(entry -> BeaconPivot.resolve(client, entry)).toArray(BeaconPivot[]::new);
        return rv;
    }

    public abstract void die();

    public abstract void tunnel();
}

