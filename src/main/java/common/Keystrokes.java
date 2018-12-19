package common;

import sleep.runtime.Scalar;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

public class Keystrokes implements Serializable,
        Transcript,
        Loggable,
        ToScalar {
    protected String when;
    protected String bid;
    protected String data;

    @Override
    public Scalar toScalar() {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("when", this.when);
        temp.put("bid", this.bid);
        temp.put("data", this.data);
        return ScriptUtils.convertAll(temp);
    }

    public Keystrokes(String bid, String data) {
        this.bid = bid;
        this.data = data;
        this.when = System.currentTimeMillis() + "";
    }

    public String id() {
        return this.bid;
    }

    public String toString() {
        return "keystrokes from beacon id: " + this.bid;
    }

    public String time() {
        return this.when;
    }

    public String getKeystrokes() {
        return this.data;
    }

    @Override
    public String getBeaconId() {
        return this.bid;
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        out.writeBytes(CommonUtils.formatDate(Long.parseLong(this.when)) + " Received keystrokes");
        out.writeBytes("\n\n");
        out.writeBytes(this.getKeystrokes());
        out.writeBytes("\n");
    }

    @Override
    public String getLogFile() {
        return "keystrokes_" + this.bid + ".txt";
    }

    @Override
    public String getLogFolder() {
        return "keystrokes";
    }
}

