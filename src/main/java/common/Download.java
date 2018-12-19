package common;

import sleep.runtime.Scalar;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Download implements Serializable,
        Transcript,
        ToScalar,
        Loggable {
    protected long date;
    protected String bid;
    protected String name;
    protected String rpath;
    protected String lpath;
    protected long size;
    protected String host;
    protected long rcvd;
    protected int fid;

    public Download(int fid, String bid, String host, String name, String rpath, String lpath, long size) {
        this.fid = fid;
        this.bid = bid;
        this.name = name;
        this.rpath = rpath;
        this.lpath = lpath;
        this.size = size;
        this.date = System.currentTimeMillis();
        this.host = host;
        this.rcvd = new File(lpath).length();
    }

    @Override
    public String getBeaconId() {
        return null;
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(this.date);
        result.append("\t");
        result.append(this.host);
        result.append("\t");
        result.append(this.bid);
        result.append("\t");
        result.append(this.size);
        result.append("\t");
        result.append(this.lpath);
        result.append("\t");
        result.append(this.name);
        result.append("\t");
        result.append(this.rpath);
        result.append("\n");
        CommonUtils.writeUTF8(out, result.toString());
    }

    @Override
    public String getLogFile() {
        return "downloads.log";
    }

    @Override
    public String getLogFolder() {
        return null;
    }

    public String id() {
        return this.bid;
    }

    public String toString() {
        return "file download";
    }

    @Override
    public Scalar toScalar() {
        return ScriptUtils.convertAll(this.toMap());
    }

    public Map toMap() {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("host", this.host);
        temp.put("name", this.name);
        temp.put("date", this.date + "");
        temp.put("path", this.rpath);
        temp.put("lpath", this.lpath);
        temp.put("size", this.size + "");
        temp.put("rcvd", this.rcvd + "");
        temp.put("fid", this.fid + "");
        return temp;
    }
}

