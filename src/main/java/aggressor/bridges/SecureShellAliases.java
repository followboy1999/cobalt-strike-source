package aggressor.bridges;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Hashtable;
import java.util.Stack;

public class SecureShellAliases implements Function,
        Environment,
        Loadable {
    protected AliasManager manager;

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Hashtable environment = si.getScriptEnvironment().getEnvironment();
        environment.put("&ssh_alias", this);
        environment.put("ssh_alias", this);
        environment.put("&fireSSHAlias", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public void bindFunction(ScriptInstance si, String type, String command, Block body) {
        SleepClosure f = new SleepClosure(si, body);
        this.manager.registerCommand(command, f);
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&fireSSHAlias")) {
            String bid = BridgeUtilities.getString(args, "");
            String command = BridgeUtilities.getString(args, "");
            String argz = BridgeUtilities.getString(args, "");
            this.manager.fireCommand(bid, command, argz);
            return SleepUtils.getEmptyScalar();
        }
        String command = BridgeUtilities.getString(args, "");
        SleepClosure f = BridgeUtilities.getFunction(args, script);
        this.manager.registerCommand(command, f);
        return SleepUtils.getEmptyScalar();
    }

    public SecureShellAliases(AliasManager m) {
        this.manager = m;
    }
}

