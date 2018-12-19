package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconExploits;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class ElevateBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public ElevateBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&beacon_exploits", this);
        Cortana.put(si, "&beacon_exploit_describe", this);
        Cortana.put(si, "&beacon_exploit_register", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&beacon_exploits")) {
            BeaconExploits exploits = DataUtils.getBeaconExploits(this.client.getData());
            return SleepUtils.getArrayWrapper(exploits.exploits());
        }
        if (name.equals("&beacon_exploit_describe")) {
            String exploit = BridgeUtilities.getString(args, "");
            BeaconExploits exploits = DataUtils.getBeaconExploits(this.client.getData());
            return SleepUtils.getScalar(exploits.getDescription(exploit));
        }
        if (name.equals("&beacon_exploit_register")) {
            String command = BridgeUtilities.getString(args, "");
            String desc = BridgeUtilities.getString(args, "");
            Scalar function2 = (Scalar) args.pop();
            BeaconExploits.Exploit exploit = (BeaconExploits.Exploit) ObjectUtilities.buildArgument(BeaconExploits.Exploit.class, function2, script);
            BeaconExploits exploits = DataUtils.getBeaconExploits(this.client.getData());
            exploits.register(command, desc, exploit);
        }
        return SleepUtils.getEmptyScalar();
    }
}

