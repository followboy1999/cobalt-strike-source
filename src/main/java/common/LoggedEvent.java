package common;

import sleep.runtime.Scalar;
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
        LoggedEvent ev = new LoggedEvent(null, original.to, (short) NOUSER_ERROR, null);
        ev.when = original.when;
        return ev;
    }

    public static LoggedEvent Join(String nick) {
        return new LoggedEvent(nick, null, (short) JOIN_EVENT, null);
    }

    public static LoggedEvent Quit(String nick) {
        return new LoggedEvent(nick, null, (short) QUIT_EVENT, null);
    }

    public static LoggedEvent Public(String nick, String text) {
        return new LoggedEvent(nick, null, (short) PUBLIC_CHAT_EVENT, text);
    }

    public static LoggedEvent Private(String from, String to, String text) {
        return new LoggedEvent(from, to, (short) PRIVATE_CHAT_EVENT, text);
    }

    public static LoggedEvent Action(String nick, String text) {
        return new LoggedEvent(nick, null, (short) ACTION_EVENT, text);
    }

    public static LoggedEvent Notify(String text) {
        return new LoggedEvent(null, null, (short) NOTIFY_EVENT, text);
    }

    public static LoggedEvent NewSite(String nick, String url, String desc) {
        return new LoggedEvent(nick, null, (short) NEW_SITE, "hosted " + desc + " @ " + url);
    }

    public static LoggedEvent BeaconInitial(BeaconEntry data) {
        if (data.isBeacon()) {
            return new LoggedEvent(null, data.getId(), (short) BEACON_INITIAL_EVENT, data.getUser() + "@" + data.getInternal() + " (" + data.getComputer() + ")");
        }

        return new LoggedEvent(null, data.getId(), (short) SSH_INITIAL_EVENT, data.getUser() + "@" + data.getInternal() + " (" + data.getComputer() + ")");
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
        Stack<Scalar> temp = new Stack<>();
        switch (this.type) {
            case PUBLIC_CHAT_EVENT:
            case ACTION_EVENT:
            case NEW_SITE:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                temp.push(SleepUtils.getScalar(this.from));
                break;
            case PRIVATE_CHAT_EVENT:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                temp.push(SleepUtils.getScalar(this.to));
                temp.push(SleepUtils.getScalar(this.from));
                break;
            case JOIN_EVENT:
            case QUIT_EVENT:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.from));
                break;
            case NOTIFY_EVENT:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                break;
            case NOUSER_ERROR:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.to));
                break;
            case BEACON_INITIAL_EVENT:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
            case SSH_INITIAL_EVENT:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
        }

        return temp;
    }

    public String eventName() {
        switch (this.type) {
            case PUBLIC_CHAT_EVENT:
                return "event_public";
            case PRIVATE_CHAT_EVENT:
                return "event_private";
            case JOIN_EVENT:
                return "event_join";
            case QUIT_EVENT:
                return "event_quit";
            case ACTION_EVENT:
                return "event_action";
            case NOTIFY_EVENT:
                return "event_notify";
            case NEW_SITE:
                return "event_newsite";
            case NOUSER_ERROR:
                return "event_nouser";
            case BEACON_INITIAL_EVENT:
                return "event_beacon_initial";
            case SSH_INITIAL_EVENT:
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
            case PUBLIC_CHAT_EVENT:
                results.append("<").append(this.from).append("> ").append(this.text).append("\n");
                break;

            case PRIVATE_CHAT_EVENT:
                return;
            case JOIN_EVENT:
                results.append("*** ").append(this.from).append(" joined\n");
                break;
            case QUIT_EVENT:
                results.append("*** ").append(this.from).append(" quit\n");
                break;
            case ACTION_EVENT:
                results.append("* ").append(this.from).append(" ").append(this.text).append("\n");
                break;
            case NEW_SITE:
                results.append("*** ").append(this.from).append(" ").append(this.text).append("\n");
                break;
            case NOTIFY_EVENT:
                results.append("*** ").append(this.text).append("\n");
                break;
            case NOUSER_ERROR:
                return;
            case BEACON_INITIAL_EVENT:
                results.append("*** initial beacon from ").append(this.text).append("\n");
                break;
            case SSH_INITIAL_EVENT:
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
        return (this.type == BEACON_INITIAL_EVENT) || (this.type == NOTIFY_EVENT) || (this.type == NEW_SITE) || (this.type == SSH_INITIAL_EVENT);
    }

    public Map archive() {
        Map<String, Object> temp = new HashMap<>();
        temp.put("when", this.when);

        if (this.type == BEACON_INITIAL_EVENT) {
            temp.put("type", "beacon_initial");
            temp.put("data", "initial beacon");
            temp.put("bid", this.to);
        } else if (this.type == SSH_INITIAL_EVENT) {
            temp.put("type", "ssh_initial");
            temp.put("data", "new ssh session");
            temp.put("bid", this.to);
        } else if ((this.type == NOTIFY_EVENT) || (this.type == NEW_SITE)) {
            temp.put("type", "notify");
            temp.put("data", this.text);
        }
        return temp;
    }
}