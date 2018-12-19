package cortana.gui;

import common.CommonUtils;
import cortana.core.EventManager;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class UIBridge implements Loadable,
        Function {
    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&later")) {
            final SleepClosure f = BridgeUtilities.getFunction(args, script);
            final Stack argz = EventManager.shallowCopy(args);
            CommonUtils.runSafe(() -> SleepUtils.runCode(f, "laterz", null, argz));
        }
        return SleepUtils.getEmptyScalar();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void scriptLoaded(ScriptInstance si) {
        si.getScriptEnvironment().getEnvironment().put("&later", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

}

