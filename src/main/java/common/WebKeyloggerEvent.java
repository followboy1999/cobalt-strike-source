package common;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

public class WebKeyloggerEvent implements Serializable,Transcript, Scriptable, Loggable {
    public String from;
    public String who;
    public String data;
    public String id;

    public WebKeyloggerEvent(String from, String who, Map parameters, String id) {
        this.from = from;
        this.who = who;
        this.data = parameters.get("data") + "";
        this.id = id;
    }

    @Override
    public Stack eventArguments() {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getScalar(this.id));
        temp.push(SleepUtils.getScalar(this.data));
        temp.push(SleepUtils.getScalar(this.who));
        temp.push(SleepUtils.getScalar(this.from));
        return temp;
    }

    @Override
    public String eventName() {
        return "keylogger_hit";
    }

    @Override
    public String getBeaconId() {
        return null;
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        out.writeBytes(CommonUtils.formatDate(System.currentTimeMillis()));
        out.writeBytes(" [HIT] " + this.from);
        out.writeBytes(", address: ");
        out.writeBytes(this.who);
        out.writeBytes(", id: ");
        out.writeBytes(this.id);
        out.writeBytes("\n");
        String[] values = this.data.split(",");
        block5:
        for (int x = 1; x < values.length; ++x) {
            int current = CommonUtils.toNumberFromHex(values[x], -1);
            switch (current) {
                case 8: {
                    out.writeBytes("<DEL>");
                    continue block5;
                }
                case 9: {
                    out.writeBytes("<TAB>");
                    continue block5;
                }
                case 10:
                case 13: {
                    out.writeBytes("<ENTER>");
                    continue block5;
                }
                default: {
                    out.writeByte((char) current);
                }
            }
        }
        out.writeBytes("\n\n");
    }

    @Override
    public String getLogFile() {
        return "webkeystrokes.log";
    }

    @Override
    public String getLogFolder() {
        return null;
    }
}

