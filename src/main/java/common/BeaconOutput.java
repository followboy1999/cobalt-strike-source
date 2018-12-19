package common;

import sleep.runtime.SleepUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class BeaconOutput implements Serializable, Transcript, Loggable, Informant, Scriptable {
    public final static short ERROR = 0;
    public final static short TASK = 1;
    public final static short OUTPUT = 2;
    public final static short CHECKIN = 3;
    public final static short INPUT = 4;
    public final static short MODE = 5;
    public final static short OUTPUT_PS = 6;
    public final static short OUTPUTB = 7;
    public final static short OUTPUT_JOBS = 8;
    public final static short OUTPUT_LS = 9;
    public final static short INDICATOR = 10;
    public final static short ACTIVITY = 11;
    public String from;
    public long when;
    public short type;
    public String text;
    public String bid;
    public String tactic;

    public boolean is(String bid) {
        return this.bid.equals(bid);
    }

    public boolean isSSH() {
        return "session".equals(CommonUtils.session(this.bid));
    }

    public boolean isBeacon() {
        return !this.isSSH();
    }

    public String prefix(String name) {
        return this.isSSH() ? "ssh_" + name : "beacon_" + name;
    }

    public String eventName() {
        switch (this.type) {
            case ERROR:
                return this.prefix("error");
            case TASK:
                return this.prefix("tasked");
            case OUTPUT:
                return this.prefix("output");
            case CHECKIN:
                return this.prefix("checkin");
            case INPUT:
                return this.prefix("input");
            case MODE:
                return this.prefix("mode");
            case OUTPUT_PS:
                return this.prefix("output_ps");
            case OUTPUTB:
                return this.prefix("output_alt");
            case OUTPUT_JOBS:
                return this.prefix("output_jobs");
            case OUTPUT_LS:
                return this.prefix("output_ls");
            case INDICATOR:
                return this.prefix("indicator");
            default:
                return this.prefix("generic");
        }
    }

    public Stack eventArguments() {
        Stack<sleep.runtime.Scalar> temp = new Stack<>();
        switch (this.type) {
            case ERROR:
            case TASK:
            case OUTPUT:
            case CHECKIN:
            case MODE:
            case OUTPUT_PS:
            case OUTPUTB:
            case OUTPUT_JOBS:
            case OUTPUT_LS:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                temp.push(SleepUtils.getScalar(this.bid));
                break;
            case INPUT:
            case INDICATOR:
                temp.push(SleepUtils.getScalar(this.when));
                temp.push(SleepUtils.getScalar(this.text));
                temp.push(SleepUtils.getScalar(this.from));
                temp.push(SleepUtils.getScalar(this.bid));
        }

        return temp;
    }

    public BeaconOutput(String bid, short type, String text) {
        this(bid, type, text, "");
    }

    public BeaconOutput(String bid, short type, String text, String tactic) {
        this.from = null;
        this.when = System.currentTimeMillis();
        this.tactic = "";
        this.type = type;
        this.text = text;
        this.bid = bid;
        this.tactic = tactic;
    }

    public static BeaconOutput Input(String bid, String text) {
        return new BeaconOutput(bid, INPUT, text);
    }

    public static BeaconOutput Mode(String bid, String text) {
        return new BeaconOutput(bid, MODE, text);
    }

    public static BeaconOutput Error(String bid, String text) {
        return new BeaconOutput(bid, ERROR, text);
    }

    public static BeaconOutput Task(String bid, String text) {
        return new BeaconOutput(bid, TASK, text);
    }

    public static BeaconOutput Task(String bid, String text, String tactic) {
        return new BeaconOutput(bid, TASK, text, tactic);
    }

    public static BeaconOutput Output(String bid, String text) {
        return new BeaconOutput(bid, OUTPUT, text);
    }

    public static BeaconOutput OutputB(String bid, String text) {
        return new BeaconOutput(bid, OUTPUTB, text);
    }

    public static BeaconOutput OutputPS(String bid, String text) {
        return new BeaconOutput(bid, OUTPUT_PS, text);
    }

    public static BeaconOutput OutputLS(String bid, String text) {
        return new BeaconOutput(bid, OUTPUT_LS, text);
    }

    public static BeaconOutput Checkin(String bid, String text) {
        return new BeaconOutput(bid, CHECKIN, text);
    }

    public static BeaconOutput OutputJobs(String bid, String text) {
        return new BeaconOutput(bid, OUTPUT_JOBS, text);
    }

    public static BeaconOutput Indicator(String bid, String text) {
        return new BeaconOutput(bid, INDICATOR, text);
    }

    public static BeaconOutput Activity(String bid, String text) {
        return new BeaconOutput(bid, ACTIVITY, text);
    }

    public static BeaconOutput FileIndicator(String bid, String name, byte[] data) {
        String result = String.format("file: %s %d bytes %s", CommonUtils.toHex(CommonUtils.MD5(data)), data.length, name);
        return Indicator(bid, result);
    }

    public static BeaconOutput ServiceIndicator(String bid, String where, String name) {
        String result = String.format("service: \\\\%s %s", where, name);
        return Indicator(bid, result);
    }

    public void touch() {
        this.when = System.currentTimeMillis();
    }

    public void user(String name) {
        this.from = name;
    }

    public String toString() {
        if (this.type == TASK) {
            return "[TASK] " + this.from + " " + this.text;
        } else if (this.type == OUTPUT) {
            return "[OUTPUT] " + this.text;
        } else {
            return this.type == ERROR ? "[ERROR] " + this.text : "Output: " + this.type;
        }
    }

    public String getBeaconId() {
        return this.bid;
    }

    public void formatEvent(DataOutputStream out) throws IOException {
        if (this.type == ACTIVITY) {
            return;
        }
        out.writeBytes(CommonUtils.formatDate(this.when));
        out.writeBytes(" ");
        switch (this.type) {
            case ERROR:
                CommonUtils.writeUTF8(out, "[error] " + this.text);
                break;
            case TASK:
                CommonUtils.writeUTF8(out, "[task] " + this.text);
                break;
            case OUTPUT:
            case OUTPUT_PS:
            case OUTPUTB:
            case OUTPUT_JOBS:
            case OUTPUT_LS:
                CommonUtils.writeUTF8(out, "[output]\n" + this.text + "\n");
                break;
            case CHECKIN:
                CommonUtils.writeUTF8(out, "[checkin] " + this.text);
                break;
            case INPUT:
                CommonUtils.writeUTF8(out, "[input] <" + this.from + "> " + this.text);
                break;
            case MODE:
                CommonUtils.writeUTF8(out, "[mode] " + this.text);
                break;
            case INDICATOR:
                CommonUtils.writeUTF8(out, "[indicator] " + this.text);
        }

        out.writeBytes("\n");
    }

    public String getLogFile() {
        return this.prefix(this.bid + ".log");
    }

    public String getLogFolder() {
        return null;
    }

    public boolean hasInformation() {
        return this.type == INDICATOR || this.type == INPUT || this.type == TASK || this.type == CHECKIN || this.type == ACTIVITY || this.type == MODE;
    }

    public Map archive() {
        Map<String, Serializable> temp = new HashMap<>();
        if (this.type == INDICATOR) {
            temp.put("type", "indicator");
            temp.put("bid", this.bid);
            temp.put("data", this.text);
            temp.put("when", this.when);
        } else if (this.type == INPUT) {
            temp.put("type", "input");
            temp.put("bid", this.bid);
            temp.put("data", this.text);
            temp.put("when", this.when);
        } else if (this.type == TASK) {
            temp.put("type", "task");
            temp.put("bid", this.bid);
            if (this.text.startsWith("Tasked beacon to ")) {
                temp.put("data", this.text.substring("Tasked beacon to ".length()));
            } else if (this.text.startsWith("Tasked session to ")) {
                temp.put("data", this.text.substring("Tasked session to ".length()));
            } else {
                temp.put("data", this.text);
            }

            temp.put("when", this.when);
        } else if (this.type == CHECKIN) {
            temp.put("type", "checkin");
            temp.put("bid", this.bid);
            temp.put("data", this.text);
            temp.put("when", this.when);
        } else if (this.type == ACTIVITY) {
            temp.put("type", "output");
            temp.put("bid", this.bid);
            temp.put("data", this.text);
            temp.put("when", this.when);
        } else if (this.type == MODE) {
            temp.put("type", "task");
            temp.put("bid", this.bid);
            temp.put("data", this.text);
            temp.put("when", this.when);
        }

        if ("".equals(this.tactic)) {
            return temp;
        }
        temp.put("tactic", this.tactic);

        return temp;
    }
}
