package common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TabScreenshot implements Serializable,
        Loggable {
    protected long when;
    protected byte[] data;
    protected String who = null;
    protected String title;
    private static final SimpleDateFormat screenFileFormat = new SimpleDateFormat("hhmmss");

    public TabScreenshot(String title, byte[] data) {
        this.title = title;
        this.data = data;
    }

    public void touch(String who) {
        this.when = System.currentTimeMillis();
        this.who = who;
    }

    public String toString() {
        return "screenshot: " + this.title;
    }

    @Override
    public String getBeaconId() {
        return null;
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        out.write(this.data);
    }

    @Override
    public String getLogFile() {
        Date temp = new Date(this.when);
        String sofar = screenFileFormat.format(temp);
        return sofar + "_" + this.title.replaceAll("[^a-zA-Z0-9\\.]", "") + ".png";
    }

    @Override
    public String getLogFolder() {
        return "screenshots/" + this.who.replaceAll("[^a-zA-Z0-9]", "");
    }
}

