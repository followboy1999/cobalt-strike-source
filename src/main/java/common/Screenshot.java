package common;

import sleep.runtime.Scalar;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Screenshot implements Serializable, Transcript, Loggable, ToScalar {
    protected String when;
    protected String bid;
    protected byte[] data;
    private static final SimpleDateFormat screenFileFormat = new SimpleDateFormat("hhmmss");

    public Screenshot(String bid, byte[] data) {
        this.bid = bid;
        this.data = data;
        this.when = System.currentTimeMillis() + "";
    }

    public String id() {
        return this.bid;
    }

    public String toString() {
        return "screenshot from beacon id: " + this.bid;
    }

    public String time() {
        return this.when;
    }

    public Icon getImage() {
        return new ImageIcon(this.data);
    }

    public String getBeaconId() {
        return this.bid;
    }

    public void formatEvent(DataOutputStream out) throws IOException {
        out.write(this.data);
    }

    public String getLogFile() {
        Date temp = new Date(Long.parseLong(this.when));
        String sofar = screenFileFormat.format(temp);
        return "screen_" + sofar + "_" + this.bid + ".jpg";
    }

    public String getLogFolder() {
        return "screenshots";
    }

    public Scalar toScalar() {
        Map<String, Object> temp = new HashMap<>();
        temp.put("bid", this.bid);
        temp.put("when", this.when);
        temp.put("data", this.data);
        return ScriptUtils.convertAll(temp);
    }
}
