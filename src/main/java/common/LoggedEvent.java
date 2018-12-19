package common;

import sleep.runtime.SleepUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LoggedEvent implements Serializable, Scriptable, Transcript, Loggable, Informant {
    public static final short PUBLIC_CHAT_EVENT = 0;
    public static final short PRIVATE_CHAT_EVENT = 1;
    public static final short JOIN_EVENT = 2;
    public static final short QUIT_EVENT = 3;
    public static final short ACTION_EVENT = 4;
    public static final short NOTIFY_EVENT = 5;
    public static final short NOUSER_ERROR = 6;
    public static final short NEW_SITE = 7;
    public static final short BEACON_INITIAL_EVENT = 8;
    public static final short SSH_INITIAL_EVENT = 9;
    public String from;
    public String to;
    public String text;
    public long when;
    public short type;

    public static LoggedEvent NoUser(LoggedEvent original) {
        LoggedEvent ev = new LoggedEvent(null, original.to, (short) 6, null);
        ev.when = original.when;
        return ev;
    }

    public static LoggedEvent Join(String nick) {
        return new LoggedEvent(nick, null, (short) 2, null);
    }

    public static LoggedEvent Quit(String nick) {
        return new LoggedEvent(nick, null, (short) 3, null);
    }

    public static LoggedEvent Public(String nick, String text) {
        return new LoggedEvent(nick, null, (short) 0, text);
    }

    public static LoggedEvent Private(String from, String to, String text) {
        return new LoggedEvent(from, to, (short) 1, text);
    }

    public static LoggedEvent Action(String nick, String text) {
        return new LoggedEvent(nick, null, (short) 4, text);
    }

    public static LoggedEvent Notify(String text) {
        return new LoggedEvent(null, null, (short) 5, text);
    }

    public static LoggedEvent NewSite(String nick, String url, String desc) {
        return new LoggedEvent(nick, null, (short) 7, "hosted " + desc + " @ " + url);
    }

    public static LoggedEvent BeaconInitial(BeaconEntry data) {
        return data.isBeacon() ? new LoggedEvent(null, data.getId(), (short) 8, data.getUser() + "@" + data.getInternal() + " (" + data.getComputer() + ")") : new LoggedEvent(null, data.getId(), (short) 9, data.getUser() + "@" + data.getInternal() + " (" + data.getComputer() + ")");
    }

    public LoggedEvent(String from, String to, short type, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.type = type;
        this.when = System.currentTimeMillis();
    }

    public void touch() {
        this.when = System.currentTimeMillis();
    }

    public Stack eventArguments() {
        Stack temp = new Stack();
        switch (this.type) {
            case 0:
            case 4:
            case 7:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                temp.push(SleepUtils.getScalar(this.from));
                break;
            case 1:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                temp.push(SleepUtils.getScalar(this.to));
                temp.push(SleepUtils.getScalar(this.from));
                break;
            case 2:
            case 3:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.from));
                break;
            case 5:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                break;
            case 6:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.to));
                break;
            case 8:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
            case 9:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
        }

        return temp;
    }

    public String eventName() {
        switch (this.type) {
            case 0:
                return "event_public";
            case 1:
                return "event_private";
            case 2:
                return "event_join";
            case 3:
                return "event_quit";
            case 4:
                return "event_action";
            case 5:
                return "event_notify";
            case 6:
                return "event_nouser";
            case 7:
                return "event_newsite";
            case 8:
                return "event_beacon_initial";
            case 9:
                return "event_ssh_initial";
            default:
                return "event_unknown";
        }
    }

    public String getBeaconId() {
        return null;
    }

    public void formatEvent(DataOutputStream out) throws IOException {
        StringBuilder results = new StringBuilder();
        results.append(CommonUtils.formatTime(this.when));
        results.append(" ");
        switch (this.type) {
            case 0:
                results.append("<").append(this.from).append("> ").append(this.text).append("\n");
                break;
            case 1:
                return;
            case 2:
                results.append("*** ").append(this.from).append(" joined\n");
                break;
            case 3:
                results.append("*** ").append(this.from).append(" quit\n");
                break;
            case 4:
                results.append("* ").append(this.from).append(" ").append(this.text).append("\n");
                break;
            case 5:
                results.append("*** ").append(this.text).append("\n");
                break;
            case 6:
                return;
            case 7:
                results.append("*** ").append(this.from).append(" ").append(this.text).append("\n");
                break;
            case 8:
                results.append("*** initial beacon from ").append(this.text).append("\n");
                break;
            case 9:
                results.append("*** new ssh session ").append(this.text).append("\n");
        }

        CommonUtils.writeUTF8(out, results.toString());
    }

    public String getLogFile() {
        return "events.log";
    }

    public String getLogFolder() {
        return null;
    }

    public boolean hasInformation() {
        return this.type == 8 || this.type == 5 || this.type == 7 || this.type == 9;
    }

    public Map archive() {
        Map temp = new HashMap();
        temp.put("when", this.when);
        if (this.type == 8) {
            temp.put("type", "beacon_initial");
            temp.put("data", "initial beacon");
            temp.put("bid", this.to);
        } else if (this.type == 9) {
            temp.put("type", "ssh_initial");
            temp.put("data", "new ssh session");
            temp.put("bid", this.to);
        } else if (this.type == 5 || this.type == 7) {
            temp.put("type", "notify");
            temp.put("data", this.text);
        }

        return temp;
    }
}
