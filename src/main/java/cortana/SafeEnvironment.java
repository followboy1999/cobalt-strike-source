package cortana;

import common.MudgeSanity;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.runtime.ScriptInstance;

public class SafeEnvironment implements Environment {
    protected Environment f;

    public SafeEnvironment(Environment f) {
        this.f = f;
    }

    @Override
    public void bindFunction(ScriptInstance si, String type, String fname, Block functionBody) {
        try {
            this.f.bindFunction(si, type, fname, functionBody);
        } catch (Exception ex) {
            MudgeSanity.logException("cortana bridge: " + type + " '" + fname + "'", ex, false);
        }
    }
}

