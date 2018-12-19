package cortana.core;

import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Loadable;
import sleep.runtime.ScriptInstance;

import java.util.Hashtable;

public class Formats implements Environment,
        Loadable {
    protected FormatManager manager;

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Hashtable environment = si.getScriptEnvironment().getEnvironment();
        environment.put("set", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    protected void register(String name, SleepClosure c) {
        this.manager.register(name, c);
    }

    @Override
    public void bindFunction(ScriptInstance si, String type, String event, Block body) {
        SleepClosure f = new SleepClosure(si, body);
        this.register(event, f);
    }

    public Formats(FormatManager m) {
        this.manager = m;
    }
}

