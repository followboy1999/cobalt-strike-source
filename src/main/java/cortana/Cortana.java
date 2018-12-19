package cortana;

import common.AObject;
import common.CommonUtils;
import cortana.core.EventManager;
import cortana.core.FormatManager;
import cortana.gui.KeyBridge;
import cortana.gui.MenuBuilder;
import cortana.gui.ScriptableApplication;
import cortana.support.CortanaUtilities;
import cortana.support.Heartbeat;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.io.IOObject;
import sleep.error.RuntimeWarningWatcher;
import sleep.error.ScriptWarning;
import sleep.error.YourCodeSucksException;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Cortana
        extends AObject implements Loadable,
        RuntimeWarningWatcher,
        Function {
    protected IOObject cortana_io = null;
    protected CortanaPipe pipe = null;
    protected ScriptableApplication application;
    protected ConsoleInterface myinterface;
    protected EventManager events = new EventManager();
    protected MenuBuilder menus;
    protected FormatManager formats = new FormatManager();
    protected Loadable utils = new CortanaUtilities();
    protected Loadable keys;
    protected LinkedList<Loadable> bridges = new LinkedList<>();
    protected boolean active = true;
    protected HashMap<String, Object> scripts = new HashMap<>();

    public List getScripts() {
        LinkedList<String> results = this.scripts.keySet().stream().filter(Objects::nonNull).map(File::new).map(File::getName).collect(Collectors.toCollection(LinkedList::new));
        return results;
    }

    public void register(Loadable l) {
        this.bridges.add(l);
    }

    public ScriptableApplication getScriptableApplication() {
        return this.application;
    }

    public boolean isActive() {
        return this.active;
    }

    public void go() {
        new Heartbeat(this).start();
    }

    public Cortana(ScriptableApplication app) {
        if (!app.isHeadless()) {
            this.pipe = new CortanaPipe();
            this.cortana_io = new IOObject();
            this.cortana_io.openWrite(this.pipe.getOutput());
        }
        this.application = app;
        this.myinterface = new ConsoleInterface(this);
        this.keys = new KeyBridge(this.application);
        this.menus = new MenuBuilder(this.application);
    }

    public String format(String name, Stack args) {
        return this.formats.format(name, args);
    }

    public static void put(ScriptInstance si, String name, Function f) {
        si.getScriptEnvironment().getEnvironment().put(name, new SafeFunction(f));
    }

    public static void putenv(ScriptInstance si, String name, Environment f) {
        si.getScriptEnvironment().getEnvironment().put(name, new SafeEnvironment(f));
    }

    public MenuBuilder getMenuBuilder() {
        return this.menus;
    }

    public EventManager getEventManager() {
        return this.events;
    }

    public void addTextListener(CortanaPipe.CortanaPipeListener l) {
        this.pipe.addCortanaPipeListener(l);
    }

    public void stop() {
        if (this.pipe != null) {
            this.pipe.close();
        }
        this.active = false;
    }

    public ConsoleInterface getConsoleInterface() {
        return this.myinterface;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        if (this.cortana_io != null) {
            IOObject.setConsole(si.getScriptEnvironment(), this.cortana_io);
        }
        si.getScriptEnvironment().getEnvironment().put("&script_load", this);
        si.getScriptEnvironment().getEnvironment().put("&script_unload", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String function2, ScriptInstance script, Stack args) {
        if (function2.equals("&script_load")) {
            try {
                this.loadScript(BridgeUtilities.getString(args, ""));
            } catch (YourCodeSucksException yex) {
                throw new RuntimeException(yex.formatErrors());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else if (function2.equals("&script_unload")) {
            String scriptf = this.findScript(BridgeUtilities.getString(args, ""));
            if (scriptf == null) {
                throw new RuntimeException("Could not find script");
            }
            this.unloadScript(scriptf);
        }
        return SleepUtils.getEmptyScalar();
    }

    @Override
    public void processScriptWarning(ScriptWarning warning) {
        String from = warning.getNameShort() + ":" + warning.getLineNumber();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date adate = new Date();
        String niced = format.format(adate, new StringBuffer(), new FieldPosition(0)).toString();
        if (warning.isDebugTrace()) {
            this.p("[" + niced + "] Trace: " + warning.getMessage() + " at " + from);
        } else {
            this.p("[" + niced + "] " + warning.getMessage() + " at " + from);
        }
    }

    public static void filterList(List l, String filter) {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            String cmd = i.next() + "";
            if (cmd.startsWith(filter)) continue;
            i.remove();
        }
    }

    public String findScript(String script) {
        for (Object o : this.scripts.keySet()) {
            String name = o.toString();
            File s = new File(name);
            if (!script.equals(s.getName())) continue;
            return name;
        }
        return null;
    }

    public void unloadScript(String file) {
        Loader loader = (Loader) this.scripts.get(file);
        if (loader == null) {
            return;
        }
        this.scripts.remove(file);
        loader.unload();
    }

    public void loadScript(String file) throws YourCodeSucksException, IOException {
        this.loadScript(file, null);
    }

    public void loadScript(String file, InputStream in) throws YourCodeSucksException, IOException {
        Loader loader = new Loader(this);
        if (this.scripts.containsKey(file)) {
            throw new RuntimeException(file + " is already loaded");
        }
        loader.getScriptLoader().addGlobalBridge(this.events.getBridge());
        loader.getScriptLoader().addGlobalBridge(this.formats.getBridge());
        loader.getScriptLoader().addGlobalBridge(this.myinterface.getBridge());
        loader.getScriptLoader().addGlobalBridge(this.utils);
        loader.getScriptLoader().addGlobalBridge(this);
        loader.getScriptLoader().addGlobalBridge(this.keys);
        loader.getScriptLoader().addGlobalBridge(this.menus.getBridge());
        for (Loadable l : this.bridges) {
            loader.getScriptLoader().addGlobalBridge(l);
        }
        if (in != null) {
            loader.loadScript(file, in);
        } else {
            loader.loadScript(file);
        }
        this.scripts.put(file, loader);
    }

    public void pgood(String text) {
        if (this.application.isHeadless()) {
            CommonUtils.print_good(text);
        } else {
            this.p("\u00039[+]\u000f " + text);
        }
    }

    public void perror(String text) {
        if (this.application.isHeadless()) {
            CommonUtils.print_error(text);
        } else {
            this.p("\u00034[-]\u000f " + text);
        }
    }

    public void pwarn(String text) {
        if (this.application.isHeadless()) {
            CommonUtils.print_warn(text);
        } else {
            this.p("\u00038[!]\u000f " + text);
        }
    }

    public void pinfo(String text) {
        if (this.application.isHeadless()) {
            CommonUtils.print_info(text);
        } else {
            this.p("\u0003C[*]\u000f " + text);
        }
    }

    public void pdark(String text) {
        if (this.application.isHeadless()) {
            System.out.println("\u001b[01;30m" + text + "\u001b[0m");
        } else {
            this.p("\u0003E" + text + '\u000f');
        }
    }

    public void p(String text) {
        if (this.cortana_io != null) {
            this.cortana_io.printLine(text);
        } else {
            System.out.println(text);
        }
    }
}

