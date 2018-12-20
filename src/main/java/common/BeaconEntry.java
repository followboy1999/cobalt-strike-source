package common;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class BeaconEntry implements Serializable, Loggable {
    public static final int LINK_NONE = 0;
    public static final int LINK_GOOD = 1;
    public static final int LINK_BROKEN = 2;
    protected String id = "";
    protected String pid = "";
    protected String ver = "";
    protected String intz = "";
    protected String comp = "";
    protected String user = "";
    protected String is64 = "0";
    protected String ext = "";
    protected long last = System.currentTimeMillis();
    protected long diff = 0L;
    protected int state = LINK_NONE;
    protected String pbid = "";
    protected String note = "";
    protected String barch = "x86";
    protected boolean alive = true;
    protected String port = "";
    protected boolean sane;
    protected String chst = null;

    public String getId() {
        return this.id;
    }

    public boolean sane() {
        return this.sane;
    }

    public String getPort() {
        return this.port;
    }

    public void die() {
        this.alive = false;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public String getComputer() {
        return this.comp;
    }

    public boolean isEmpty() {
        return this.intz == null || this.intz.length() == 0;
    }

    public String getUser() {
        return this.user;
    }

    public String getInternal() {
        return this.intz;
    }

    public String getExternal() {
        return this.ext;
    }

    public String getPid() {
        if (this.isSSH()) {
            return "";
        }
        return this.pid;
    }

    public double getVersion() {
        try {
            if (this.isSSH() && this.ver.startsWith("ssh-CYGWIN_NT-")) {
                return Double.parseDouble(CommonUtils.strip(this.ver, "ssh-CYGWIN_NT-"));
            }
            if (this.isBeacon()) {
                return Double.parseDouble(this.ver);
            }
            return 0.0;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    public String getNote() {
        return this.note;
    }

    public String getParentId() {
        return this.pbid;
    }

    public boolean isLinked() {
        return this.pbid.length() > 0;
    }

    public int getLinkState() {
        return this.state;
    }

    public String arch() {
        return this.barch;
    }

    public boolean is64() {
        if (!this.is64.equals("1") && !this.is64.equals("0")) {
            CommonUtils.print_warn("is64 is: '" + this.is64 + "'");
        }
        return this.is64.equals("1");
    }

    public boolean isAdmin() {
        return this.getUser().endsWith(" *");
    }

    public void setExternal(String e) {
        if (this.checkExt(e)) {
            this.ext = e;
        } else {
            CommonUtils.print_error("Refused to assign: '" + e + "' [was: '" + this.ext + "'] as external address to Beacon: '" + this.id + "'");
        }
    }

    public void setLastCheckin(long ci) {
        this.last = ci;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean idle(long time) {
        return this.diff >= time;
    }

    public String getLastCheckin() {
        String units = "ms";
        long diffz = this.diff;
        if (diffz <= 1000L) {
            return diffz + units;
        }
        units = "s";
        if ((diffz /= 1000L) > 60L) {
            diffz /= 60L;
            units = "m";
        }
        if (diffz > 60L) {
            diffz /= 60L;
            units = "h";
        }
        return diffz + units;
    }

    public BeaconEntry(byte[] metadatab, String charset, String external) {
        String metadata = CommonUtils.bString(Arrays.copyOfRange(metadatab, 20, metadatab.length), charset);
        String[] mdata = metadata.split("\t");
        if (mdata.length > 0) {
            this.id = mdata[0];
        }
        if (mdata.length > 1) {
            this.pid = mdata[1];
        }
        if (mdata.length > 2) {
            this.ver = mdata[2];
        }
        if (mdata.length > 3) {
            this.intz = mdata[3];
        }
        if (mdata.length > 4) {
            this.comp = mdata[4];
        }
        if (mdata.length > 5) {
            this.user = mdata[5];
        }
        if (mdata.length > 6) {
            this.is64 = mdata[6];
        }
        if (mdata.length > 7) {
            String string = this.barch = "1".equals(mdata[7]) ? "x64" : "x86";
        }
        if (mdata.length > 8) {
            this.port = mdata[8];
        }
        this.ext = external;
        this.chst = charset;
        this.sane = this.sanity();
    }

    public String getCharset() {
        return this.chst;
    }

    public boolean sanity() {
        LinkedList<String> messages = new LinkedList<>();
        try {
            return this._sanity(messages);
        } catch (Exception ex) {
            this.id = "0";
            this.intz = "";
            MudgeSanity.logException("Validator blew up!", ex, false);
            return false;
        }
    }

    public boolean checkExt(String ext) {
        if (ext == null) {
            return true;
        }
        if ("".equals(ext)) {
            return true;
        }
        String check;
        check = ext.endsWith(" \u26af \u26af") && ext.length() > 5 ? ext.substring(0, ext.length() - 4) : (ext.endsWith(" \u26af\u26af") && ext.length() > 4 ? ext.substring(0, ext.length() - 3) : ext);
        return CommonUtils.isIP(check) || CommonUtils.isIPv6(check) || "unknown".equals(check);
    }

    public boolean _sanity(LinkedList<String> messages) {
        if (!CommonUtils.isNumber(this.id)) {
            messages.add("id '" + this.id + "' is not a number");
            this.id = "0";
        }
        if (!("".equals(this.intz) || CommonUtils.isIP(this.intz) || CommonUtils.isIPv6(this.intz) || "unknown".equals(this.intz))) {
            messages.add("internal address '" + this.intz + "' is not an address");
            this.intz = "";
        }
        if (!this.checkExt(this.ext)) {
            messages.add("external address '" + this.ext + "' is not an address");
            this.ext = "";
        }
        if (!"".equals(this.pid) && !CommonUtils.isNumber(this.pid)) {
            messages.add("pid '" + this.pid + "' is not a number");
            this.pid = "0";
        }
        if (!"".equals(this.port) && !CommonUtils.isNumber(this.port)) {
            messages.add("port '" + this.port + "' is not a number");
            this.port = "";
        }
        if (!"".equals(this.is64) && !CommonUtils.isNumber(this.is64)) {
            messages.add("is64 '" + this.is64 + "' is not a number");
            this.is64 = "";
        }
        if (this.comp != null && this.comp.length() > 64) {
            messages.add("comp '" + this.comp + "' is too long. Truncating");
            this.comp = this.comp.substring(0, 63);
        }
        if (this.user != null && this.user.length() > 64) {
            messages.add("user '" + this.user + "' is too long. Truncating");
            this.user = this.user.substring(0, 63);
        }
        if (messages.size() > 0) {
            Iterator i = messages.iterator();
            CommonUtils.print_error("Beacon entry did not validate");
            while (i.hasNext()) {
                System.out.println("\t" + i.next());
            }
            return false;
        }
        return true;
    }

    public BeaconEntry(String id) {
        this.id = id;
        this.sane = this.sanity();
    }

    public void touch() {
        this.diff = System.currentTimeMillis() - this.last;
    }

    public BeaconEntry copy() {
        BeaconEntry entry = new BeaconEntry(this.id);
        entry.pid = this.pid;
        entry.ver = this.ver;
        entry.intz = this.intz;
        entry.comp = this.comp;
        entry.user = this.user;
        entry.is64 = this.is64;
        entry.ext = this.ext;
        entry.diff = this.diff;
        entry.last = this.last;
        entry.state = this.state;
        entry.pbid = this.pbid;
        entry.note = this.note;
        entry.alive = this.alive;
        entry.barch = this.barch;
        entry.port = this.port;
        entry.chst = this.chst;
        return entry;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("external", this.ext);
        metadata.put("internal", this.intz);
        metadata.put("host", this.intz);
        metadata.put("user", this.user);
        metadata.put("computer", this.comp);
        metadata.put("last", this.diff + "");
        metadata.put("lastf", this.getLastCheckin());
        metadata.put("id", this.id);
        metadata.put("pid", this.getPid());
        metadata.put("is64", this.is64);
        metadata.put("pbid", this.pbid);
        metadata.put("note", this.note);
        metadata.put("barch", this.barch);
        metadata.put("port", this.getPort());
        metadata.put("charset", this.getCharset());
        if (this.alive) {
            metadata.put("alive", "true");
        } else {
            metadata.put("alive", "false");
        }
        if (this.state != LINK_NONE) {
            if (this.state == LINK_GOOD) {
                metadata.put("state", "good");
            } else if (this.state == LINK_BROKEN) {
                metadata.put("state", "broken");
            }
        }
        metadata.put("os", this.getOperatingSystem());
        metadata.put("ver", Double.toString(this.getVersion()));
        if (this.isSSH()) {
            metadata.put("session", "ssh");
        } else if (this.isBeacon()) {
            metadata.put("session", "beacon");
        } else {
            metadata.put("session", "unknown");
        }
        return metadata;
    }

    public boolean wantsMetadata() {
        return this.user.length() == 0;
    }

    public String title() {
        if (this.isBeacon()) {
            return this.title("Beacon");
        }
        return "SSH " + this.intz;
    }

    public String title(String verb) {
        return verb + " " + this.intz + ":" + this.pid;
    }

    public String toString() {
        return this.getId() + " -> " + this.title() + ", " + this.getLastCheckin();
    }

    public Stack eventArguments() {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getHashWrapper(this.toMap()));
        temp.push(SleepUtils.getScalar(this.id));
        return temp;
    }

    public void link(String pbid) {
        this.pbid = pbid;
        this.state = LINK_GOOD;
    }

    public void delink() {
        this.state = LINK_BROKEN;
    }

    @Override
    public String getBeaconId() {
        return this.id;
    }

    @Override
    public String getLogFile() {
        if (this.isSSH()) {
            return "ssh_" + this.id + ".log";
        }
        return "beacon_" + this.id + ".log";
    }

    @Override
    public String getLogFolder() {
        return null;
    }

    public boolean isBeacon() {
        return !this.isSSH();
    }

    public boolean isSSH() {
        return this.ver.startsWith("ssh-");
    }

    public String getOperatingSystem() {
        if (this.isBeacon()) {
            return "Windows";
        }
        if ("ssh-".equals(this.ver)) {
            return "Unknown";
        }
        if ("ssh-Darwin".equals(this.ver)) {
            return "MacOS X";
        }
        if (this.ver.startsWith("ssh-CYGWIN_NT-")) {
            return "Windows";
        }
        return this.ver.substring(4);
    }

    @Override
    public void formatEvent(DataOutputStream out) throws IOException {
        out.writeBytes(CommonUtils.formatDate(System.currentTimeMillis()));
        out.writeBytes(" ");
        out.writeBytes("[metadata] ");
        if (this.isLinked()) {
            out.writeBytes("beacon_" + this.getParentId() + " -> " + this.getInternal() + "; ");
        } else if ("".equals(this.getExternal())) {
            out.writeBytes("unknown <- " + this.getInternal() + "; ");
        } else {
            out.writeBytes(this.getExternal() + " <- " + this.getInternal() + "; ");
        }
        if (this.isSSH()) {
            CommonUtils.writeUTF8(out, "computer: " + this.getComputer() + "; ");
            CommonUtils.writeUTF8(out, "user: " + this.getUser() + "; ");
            out.writeBytes("os: " + this.getOperatingSystem() + "; ");
            out.writeBytes("port: " + this.getPort());
        } else {
            CommonUtils.writeUTF8(out, "computer: " + this.getComputer() + "; ");
            CommonUtils.writeUTF8(out, "user: " + this.getUser() + "; ");
            out.writeBytes("pid: " + this.getPid() + "; ");
            out.writeBytes("os: " + this.getOperatingSystem() + "; ");
            out.writeBytes("version: " + this.getVersion() + "; ");
            out.writeBytes("beacon arch: " + this.barch);
            if (this.is64()) {
                out.writeBytes(" (x64)");
            }
        }
        out.writeBytes("\n");
    }
}

