package cortana.core;

import common.ScriptUtils;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class FormatManager {
    protected Map formats = new HashMap();

    public Loadable getBridge() {
        return new Formats(this);
    }

    public void register(String name, SleepClosure c) {
        this.formats.put(name, c);
    }

    public String format(String format, Stack args) {
        SleepClosure formatz = (SleepClosure) this.formats.get(format);
        if (formatz == null) {
            return null;
        }
        if (!formatz.getOwner().isLoaded()) {
            return null;
        }
        Scalar temp = SleepUtils.runCode(formatz, format, null, args);
        if (SleepUtils.isEmptyScalar(temp)) {
            return null;
        }
        return temp.toString();
    }

    public String format(String format, Object[] data) {
        Stack<Scalar> args = new Stack<>();
        int offset = data.length - 1;
        for (int x = 0; x < data.length; ++x) {
            args.push(ScriptUtils.convertAll(data[offset - x]));
        }
        return this.format(format, args);
    }
}

