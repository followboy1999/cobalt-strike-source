package cortana.gui;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;
import ui.KeyHandler;

import java.util.Stack;

public class KeyBridge implements Loadable,
        Function,
        Environment {
    protected ScriptableApplication application;

    public KeyBridge(ScriptableApplication app) {
        this.application = app;
    }

    protected void registerKey(String combination, SleepClosure closure) {
        Binding b = new Binding(closure);
        this.application.bindKey(combination, b);
    }

    @Override
    public void bindFunction(ScriptInstance si, String type, String event, Block body) {
        SleepClosure f = new SleepClosure(si, body);
        this.registerKey(event, f);
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        String desc = BridgeUtilities.getString(args, "");
        SleepClosure f = BridgeUtilities.getFunction(args, script);
        this.registerKey(desc, f);
        return SleepUtils.getEmptyScalar();
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        si.getScriptEnvironment().getEnvironment().put("bind", this);
        si.getScriptEnvironment().getEnvironment().put("&bind", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    private static class Binding implements KeyHandler {
        protected SleepClosure code;

        public Binding(SleepClosure c) {
            this.code = c;
        }

        @Override
        public void key_pressed(String description) {
            if (this.code != null && this.code.getOwner().isLoaded()) {
                SleepUtils.runCode(this.code, description, null, new Stack());
            } else {
                this.code = null;
            }
        }
    }

}

