package cortana.support;

import common.CommonUtils;
import cortana.core.EventManager;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.KeyValuePair;
import sleep.bridges.SleepClosure;
import sleep.bridges.io.IOObject;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptVariables;
import sleep.runtime.SleepUtils;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;

public class CortanaUtilities implements Function,
        Loadable {
    @Override
    public void scriptLoaded(ScriptInstance script) {
        script.getScriptEnvironment().getEnvironment().put("&spawn", this);
        script.getScriptEnvironment().getEnvironment().put("&fork", this);
        script.getScriptEnvironment().getEnvironment().put("&dispatch_event", this);
        script.getScriptEnvironment().getEnvironment().put("&apply", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance script) {
    }

    public void installVars(ScriptVariables vars, ScriptInstance script) {
        ScriptVariables original = script.getScriptVariables();
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&fork")) {
            SleepClosure param = BridgeUtilities.getFunction(args, script);
            ScriptInstance child = script.fork();
            child.installBlock(param.getRunnableCode());
            ScriptVariables vars = child.getScriptVariables();
            while (!args.isEmpty()) {
                KeyValuePair kvp = BridgeUtilities.getKeyValuePair(args);
                vars.putScalar(kvp.getKey().toString(), SleepUtils.getScalar(kvp.getValue()));
            }
            this.installVars(vars, script);
            IOObject parent_io = new IOObject();
            IOObject child_io = new IOObject();
            try {
                PipedInputStream parent_in = new PipedInputStream();
                PipedOutputStream parent_out = new PipedOutputStream();
                parent_in.connect(parent_out);
                PipedInputStream child_in = new PipedInputStream();
                PipedOutputStream child_out = new PipedOutputStream();
                child_in.connect(child_out);
                parent_io.openRead(child_in);
                parent_io.openWrite(parent_out);
                child_io.openRead(parent_in);
                child_io.openWrite(child_out);
                child.getScriptVariables().putScalar("$source", SleepUtils.getScalar(child_io));
                Thread temp = new Thread(child, "fork of " + child.getRunnableBlock().getSourceLocation());
                parent_io.setThread(temp);
                child_io.setThread(temp);
                child.setParent(parent_io);
                temp.start();
            } catch (Exception ex) {
                script.getScriptEnvironment().flagError(ex);
            }
            return SleepUtils.getScalar(parent_io);
        }
        if (name.equals("&spawn")) {
            SleepClosure param = BridgeUtilities.getFunction(args, script);
            ScriptInstance child = script.fork();
            child.installBlock(param.getRunnableCode());
            Map meta = Collections.synchronizedMap(new HashMap(script.getMetadata()));
            child.getScriptVariables().getGlobalVariables().putScalar("__meta__", SleepUtils.getScalar(meta));
            child.getMetadata().put("%scriptid%", (long) child.hashCode() ^ System.currentTimeMillis() * 13L);
            ScriptVariables vars = child.getScriptVariables();
            while (!args.isEmpty()) {
                KeyValuePair kvp = BridgeUtilities.getKeyValuePair(args);
                vars.putScalar(kvp.getKey().toString(), SleepUtils.getScalar(kvp.getValue()));
            }
            this.installVars(vars, script);
            return child.runScript();
        }
        if (name.equals("&dispatch_event")) {
            final SleepClosure param = BridgeUtilities.getFunction(args, script);
            final Stack argz = EventManager.shallowCopy(args);
            CommonUtils.runSafe(() -> SleepUtils.runCode(param, "&dispatch_event", null, argz));
        } else if (name.equals("&apply")) {
            String temp = BridgeUtilities.getString(args, "");
            if (temp.length() == 0 || temp.charAt(0) != '&') {
                throw new IllegalArgumentException(name + ": requested function name must begin with '&'");
            }
            Function f = script.getScriptEnvironment().getFunction(temp);
            if (f == null) {
                throw new RuntimeException("Function '" + temp + "' does not exist");
            }
            Stack argz = new Stack();
            Iterator i = BridgeUtilities.getIterator(args, script);
            while (i.hasNext()) {
                argz.add(0, i.next());
            }
            return SleepUtils.runCode(f, temp, script, argz);
        }
        return SleepUtils.getEmptyScalar();
    }

}

