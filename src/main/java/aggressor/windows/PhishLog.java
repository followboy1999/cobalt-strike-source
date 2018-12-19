package aggressor.windows;

import aggressor.DataManager;
import aggressor.WindowCleanup;
import common.*;
import console.ActivityConsole;
import console.Console;
import console.ConsolePopup;
import cortana.Cortana;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class PhishLog
        extends AObject implements ConsolePopup,
        Callback,
        ActionListener {
    protected Console console;
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected WindowCleanup state = null;
    protected String sid;

    public PhishLog(String sid, DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.sid = sid;
        this.console = new ActivityConsole(false);
        this.console.updatePrompt("");
        this.console.getInput().setEditable(false);
        data.subscribe("phishlog." + sid, this);
        data.subscribe("phishstatus." + sid, this);
        this.console.setPopupMenu(this);
    }

    public ActionListener cleanup() {
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        this.data.unsub("phishlog." + this.sid, this);
        this.data.unsub("phishstatus." + this.sid, this);
        this.conn.call("cloudstrike.stop_phish", CommonUtils.args(this.sid));
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
        if (key.startsWith("phishstatus")) {
            this.console.updatePrompt(o + "");
        } else {
            this.console.append(this.format(key, o));
        }
    }

    @Override
    public void showPopup(String word, MouseEvent ev) {
        this.engine.getMenuBuilder().installMenu(ev, "phishlog", new Stack());
    }
}

