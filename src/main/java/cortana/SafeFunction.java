package cortana;

import common.MudgeSanity;
import sleep.interfaces.Function;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class SafeFunction implements Function {
    protected Function f;

    public SafeFunction(Function f) {
        this.f = f;
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        try {
            return this.f.evaluate(name, script, args);
        } catch (Exception ex) {
            MudgeSanity.logException("cortana bridge: " + name, ex, false);
            if (script != null && ex != null) {
                script.getScriptEnvironment().showDebugMessage("Function call " + name + " failed: " + ex.getMessage());
            }
            return SleepUtils.getEmptyScalar();
        }
    }
}

