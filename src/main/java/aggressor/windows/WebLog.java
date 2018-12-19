package aggressor.windows;

import aggressor.DataManager;
import aggressor.WindowCleanup;
import common.AObject;
import common.Callback;
import common.Scriptable;
import common.TeamQueue;
import console.ActivityConsole;
import console.Console;
import console.ConsolePopup;
import cortana.Cortana;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class WebLog
        extends AObject implements ConsolePopup,
        Callback {
    protected Console console;
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected String nick = null;
    protected WindowCleanup state = null;

    public WebLog(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.console = new ActivityConsole(false);
        this.console.updatePrompt("> ");
        StringBuilder previous = new StringBuilder();
        for (Object o : data.getTranscriptSafe("weblog")) {
            previous.append(this.format("weblog", o));
        }
        this.console.append(previous.toString());
        data.subscribe("weblog", this);
        this.console.setPopupMenu(this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("weblog", this);
    }

    public Console getConsole() {
        return this.console;
    }

    public String format(String _key, Object o) {
        Scriptable ev = (Scriptable) o;
        return this.engine.format(ev.eventName().toUpperCase(), ev.eventArguments());
    }

    @Override
    public void result(String key, Object o) {
        this.console.append(this.format(key, o));
    }

    @Override
    public void showPopup(String word, MouseEvent ev) {
        this.engine.getMenuBuilder().installMenu(ev, "weblog", new Stack());
    }
}

