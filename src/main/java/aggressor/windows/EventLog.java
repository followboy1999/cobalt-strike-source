package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.WindowCleanup;
import common.*;
import console.*;
import cortana.Cortana;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;

public class EventLog extends AObject implements ActionListener, ConsolePopup, Callback, Do {
    protected Console console;
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected String nick;
    protected WindowCleanup state;
    protected String lag = "??";

    protected Stack<Scalar> sbarArgs(String lag) {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getScalar(lag));
        return temp;
    }

    public EventLog(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.nick = DataUtils.getNick(data);
        this.console = new ActivityConsole(true);
        this.console.updatePrompt(Colors.underline("event") + "> ");
        this.console.getInput().addActionListener(this);
        String left = engine.format("EVENT_SBAR_LEFT", this.sbarArgs("00"));
        String right = engine.format("EVENT_SBAR_RIGHT", this.sbarArgs("00"));
        this.console.getStatusBar().set(left, right);
        StringBuilder previous = new StringBuilder();
        for (Object o : data.getTranscriptAndSubscribeSafe("eventlog", this)) {
            previous.append(this.format("eventlog", o));
        }
        this.console.append(previous.toString());
        this.state = data.unsubOnClose("eventlog", this);
        new EventLogTabCompleter();
        this.console.setPopupMenu(this);
        Timers.getTimers().every(1000L, "time", this);
        Timers.getTimers().every(10000L, "lag", this);
    }

    @Override
    public boolean moment(String msg) {
        if ("time".equals(msg) && this.console.isShowing()) {
            String arg = CommonUtils.padr(this.lag, "0", 2);
            String left = this.engine.format("EVENT_SBAR_LEFT", this.sbarArgs(arg));
            String right = this.engine.format("EVENT_SBAR_RIGHT", this.sbarArgs(arg));
            this.console.getStatusBar().set(left, right);
        } else if ("lag".equals(msg)) {
            this.lag = "??";
            this.conn.call("aggressor.ping", CommonUtils.args(System.currentTimeMillis()), (key, o) -> {
                Long res = (Long) o;
                EventLog.this.lag = (int) ((double) (System.currentTimeMillis() - res) / 1000.0) + "";
            });
        }
        return this.state.isOpen();
    }

    public ActionListener cleanup() {
        return this.state;
    }

    public Console getConsole() {
        return this.console;
    }

    @Override
    public void result(String key, Object o) {
        this.console.append(this.format(key, o));
    }

    public String format(String key, Object o) {
        LoggedEvent e = (LoggedEvent) o;
        String evt = e.eventName();
        Stack args = e.eventArguments();
        String text = this.engine.format(evt.toUpperCase(), args);
        if (text == null) {
            return "";
        }
        return text + "\n";
    }

    @Override
    public void showPopup(String word, MouseEvent ev) {
        this.engine.getMenuBuilder().installMenu(ev, "eventlog", new Stack());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String text = ev.getActionCommand();
        ((JTextField) ev.getSource()).setText("");
        CommandParser parser = new CommandParser(text);
        if (parser.is("/msg")) {
            if (parser.verify("AZ")) {
                String message = parser.popString();
                String target = parser.popString();
                this.conn.call("aggressor.event", CommonUtils.args(LoggedEvent.Private(this.nick, target, message)), null);
            }
        } else if (parser.is("/names") || parser.is("/sc")) {
            LinkedList users = new LinkedList(DataUtils.getUsers(this.data));
            Collections.sort(users);
            Stack<Scalar> temp = new Stack<>();
            temp.push(SleepUtils.getArrayWrapper(users));
            this.console.append(this.engine.format("EVENT_USERS", temp) + "\n");
        } else if (parser.is("/me")) {
            if (parser.verify("Z")) {
                this.conn.call("aggressor.event", CommonUtils.args(LoggedEvent.Action(this.nick, parser.popString())), null);
            }
        } else if (text.length() > 0) {
            this.conn.call("aggressor.event", CommonUtils.args(LoggedEvent.Public(this.nick, text)), null);
        }
    }

    private class EventLogTabCompleter
            extends GenericTabCompletion {
        public EventLogTabCompleter() {
            super(EventLog.this.console);
        }

        @Override
        public Collection getOptions(String text) {
            LinkedList users = new LinkedList(DataUtils.getUsers(EventLog.this.data));
            LinkedList<String> options = new LinkedList<>();
            options.add("/me");
            options.add("/msg");
            options.add("/names");
            options.add("/sc");
            for (Object user : users) {
                String u = user + "";
                if (text.contains(" ")) {
                    options.add("/msg " + u);
                }
                options.add(u);
            }
            Collections.sort(options);
            Cortana.filterList(options, text);
            return options;
        }
    }

}

