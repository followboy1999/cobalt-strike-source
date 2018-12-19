package common;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class PhishEvent implements Serializable,
        Scriptable,
        Loggable,
        Informant {
    protected LinkedList variables;
    protected long when = System.currentTimeMillis();
    protected String evname;
    protected String sid;
    protected String desc;
    protected Map info;

    public PhishEvent(String sid, String evname, LinkedList vars, String desc, Map info) {
        this.variables = new LinkedList(vars);
        this.sid = sid;
        this.evname = evname;
        this.desc = desc;
        this.info = info;
    }

    @Override
    public Stack eventArguments() {
        Stack<Scalar> temp = new Stack<>();
        for (Object next : this.variables) {
            if (next == null) {
                temp.add(0, SleepUtils.getEmptyScalar());
                continue;
            }
            if (next instanceof Map) {
                temp.add(0, SleepUtils.getHashWrapper((Map) next));
                continue;
            }
            if (next instanceof Long) {
                temp.add(0, SleepUtils.getScalar(next));
                continue;
            }
            temp.add(0, SleepUtils.getScalar(next.toString()));
        }
        return temp;
    }

    @Override
    public String eventName() {
        return this.evname;
    }

    @Override
    public String getBeaconId() {
        return null;
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        out.writeBytes(CommonUtils.formatDate(this.when));
        out.writeBytes(" ");
        out.writeBytes(this.desc + "\n");
    }

    @Override
    public String getLogFile() {
        return "campaign_" + this.sid + ".log";
    }

    @Override
    public String getLogFolder() {
        return "phishes";
    }

    @Override
    public boolean hasInformation() {
        return this.info != null;
    }

    @Override
    public Map archive() {
        return this.info;
    }
}

