package cortana.core;

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

public class Events implements Function,
        Environment,
        Loadable {
    protected EventManager manager;

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Hashtable environment = si.getScriptEnvironment().getEnvironment();
        environment.put("&on", this);
        environment.put("on", this);
        environment.put("&when", this);
        environment.put("when", this);
        environment.put("&fireEvent", this);
        environment.put("&fire_event_local", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    protected void addListener(String name, SleepClosure c, boolean temp) {
        this.manager.addListener(name, c, temp);
    }

    @Override
    public void bindFunction(ScriptInstance si, String type, String event, Block body) {
        boolean temporary = type.equals("when");
        SleepClosure f = new SleepClosure(si, body);
        this.addListener(event, f, temporary);
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&fireEvent")) {
            String event = BridgeUtilities.getString(args, "");
            this.manager.fireEvent(event, EventManager.shallowCopy(args));
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&fire_event_local")) {
            String event = BridgeUtilities.getString(args, "");
            return SleepUtils.getEmptyScalar();
        }
        boolean temporary = name.equals("&when");
        String event = BridgeUtilities.getString(args, "");
        SleepClosure f = BridgeUtilities.getFunction(args, script);
        this.addListener(event, f, temporary);
        return SleepUtils.getEmptyScalar();
    }

    public Events(EventManager m) {
        this.manager = m;
    }
}

