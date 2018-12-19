package cortana;

import sleep.engine.Block;
import sleep.error.RuntimeWarningWatcher;
import sleep.error.YourCodeSucksException;
import sleep.interfaces.Loadable;
import sleep.runtime.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Loader implements Loadable {
    protected ScriptLoader loader = new ScriptLoader();
    protected Hashtable shared = new Hashtable();
    protected ScriptVariables vars = new ScriptVariables();
    protected Object[] passMe = new Object[4];
    protected List scripts = new LinkedList();
    protected RuntimeWarningWatcher watcher;

    public void unsetDebugLevel(int flag) {
        for (Object script1 : this.scripts) {
            ScriptInstance script = (ScriptInstance) script1;
            int flags = script.getDebugFlags() & ~flag;
            script.setDebugFlags(flags);
        }
    }

    public void printProfile(OutputStream out) {
        Iterator i = this.scripts.iterator();
        if (i.hasNext()) {
            ScriptInstance script = (ScriptInstance) i.next();
            script.printProfileStatistics(out);
            return;
        }
    }

    public void setDebugLevel(int flag) {
        for (Object script1 : this.scripts) {
            ScriptInstance script = (ScriptInstance) script1;
            int flags = script.getDebugFlags() | flag;
            script.setDebugFlags(flags);
        }
    }

    public boolean isReady() {
        synchronized (this) {
            return this.passMe != null;
        }
    }

    public void passObjects(Object o, Object p, Object q, Object r) {
        synchronized (this) {
            this.passMe[0] = o;
            this.passMe[1] = p;
            this.passMe[2] = q;
            this.passMe[3] = r;
        }
    }

    public Object[] getPassedObjects() {
        synchronized (this) {
            return this.passMe;
        }
    }

    public void setGlobal(String name, Scalar value) {
        this.vars.getGlobalVariables().putScalar(name, value);
    }

    public ScriptLoader getScriptLoader() {
        return this.loader;
    }

    public Loader(RuntimeWarningWatcher watcher) {
        this.loader.addSpecificBridge(this);
        this.watcher = watcher;
    }

    @Override
    public void scriptLoaded(ScriptInstance i) {
        i.setScriptVariables(this.vars);
        i.addWarningWatcher(this.watcher);
        this.scripts.add(i);
        i.getMetadata().put("%scriptid%", i.hashCode());
    }

    public void unload() {
        for (Object script : this.scripts) {
            ScriptInstance temp = (ScriptInstance) script;
            temp.setUnloaded();
        }
        this.scripts = null;
        this.vars = null;
        this.shared = null;
        this.passMe = null;
        this.loader = null;
    }

    @Override
    public void scriptUnloaded(ScriptInstance i) {
    }

    public Object loadInternalScript(String file, Object cache) {
        try {
            if (cache == null) {
                InputStream i = this.getClass().getClassLoader().getResourceAsStream(file);
                if (i == null) {
                    throw new RuntimeException("resource " + file + " does not exist");
                }
                cache = this.loader.compileScript(file, i);
            }
            ScriptInstance script = this.loader.loadScript(file, (Block) cache, this.shared);
            script.runScript();
        } catch (IOException ex) {
            System.err.println("*** Could not load: " + file + " - " + ex.getMessage());
        } catch (YourCodeSucksException ex) {
            ex.printErrors(System.out);
        }
        return cache;
    }

    public ScriptInstance loadScript(String file) throws IOException {
        this.setGlobal("$__script__", SleepUtils.getScalar(file));
        ScriptInstance script = this.loader.loadScript(file, this.shared);
        script.runScript();
        return script;
    }

    public ScriptInstance loadScript(String file, InputStream in) throws IOException {
        this.setGlobal("$__script__", SleepUtils.getScalar(file));
        ScriptInstance script = this.loader.loadScript(file, in, this.shared);
        script.runScript();
        return script;
    }
}

