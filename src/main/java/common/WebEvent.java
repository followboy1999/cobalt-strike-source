package common;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class WebEvent implements Serializable, Transcript, Scriptable, Loggable,Informant {
    public String method;
    public String addr;
    public String ua;
    public String from;
    public Map params;
    public String handler;
    public long when = System.currentTimeMillis();
    public String response;
    public long size;
    public String uri;

    public WebEvent(String method, String uri, String addr, String ua, String from, String handler, Map parameters, String response, long size) {
        this.method = method;
        this.uri = uri;
        this.addr = addr;
        this.ua = ua;
        this.from = from;
        this.handler = handler;
        this.params = parameters;
        this.response = response;
        this.size = size;
        this.params.remove("input");
    }

    @Override
    public Stack eventArguments() {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getScalar(this.when));
        temp.push(SleepUtils.getHashWrapper(this.params));
        temp.push(SleepUtils.getScalar(this.handler));
        temp.push(SleepUtils.getScalar(this.size));
        temp.push(SleepUtils.getScalar(this.response));
        temp.push(SleepUtils.getScalar(this.ua));
        temp.push(SleepUtils.getScalar(this.addr));
        temp.push(SleepUtils.getScalar(this.uri));
        temp.push(SleepUtils.getScalar(this.method));
        return temp;
    }

    @Override
    public String eventName() {
        return "web_hit";
    }

    @Override
    public String getBeaconId() {
        return null;
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(this.addr);
        result.append(" ");
        if (this.from != null && "unknown".equals(this.from)) {
            result.append(this.from);
            result.append(" ");
            result.append(this.from);
            result.append(" [");
        } else {
            result.append("- - [");
        }
        result.append(CommonUtils.formatDate(this.when));
        result.append("] \"");
        result.append(this.method);
        result.append(" ");
        result.append(this.uri);
        result.append("\" ");
        result.append(this.response.split(" ")[0]);
        result.append(" ");
        result.append(this.size);
        result.append(" \"");
        if (this.handler != null) {
            result.append(this.handler);
        }
        result.append("\" \"");
        result.append(this.ua);
        result.append("\"\n");
        out.writeBytes(result.toString());
    }

    @Override
    public String getLogFile() {
        return "weblog.log";
    }

    @Override
    public String getLogFolder() {
        return null;
    }

    @Override
    public boolean hasInformation() {
        return this.response.startsWith("200") && !"".equals(this.handler);
    }

    @Override
    public Map archive() {
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("when", this.when);
        temp.put("type", "webhit");
        temp.put("data", "visit to " + this.uri + " (" + this.handler + ") by " + this.addr);
        if (this.params.containsKey("id")) {
            temp.put("token", this.params.get("id"));
        }
        return temp;
    }
}

