package aggressor;

import common.CommonUtils;
import common.MudgeSanity;
import common.RangeList;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Prefs {
    protected static final Prefs prefs = new Prefs();
    protected Properties data = null;

    protected File myFile() {
        return new File(System.getProperty("user.home"), ".aggressor.prop");
    }

    protected Prefs() {
    }

    public void load() {
        if (this.data != null) {
            return;
        }
        File file = this.myFile();
        try {
            this.data = new Properties();
            InputStream in;
            in = file.exists() ? new FileInputStream(file) : CommonUtils.resource("resources/aggressor.prop");
            this.data.load(in);
            in.close();
        } catch (IOException ioex) {
            MudgeSanity.logException("Load Preferences: " + file, ioex, false);
        }
    }

    public void scrub() {
        try {
            LinkedList servers = new LinkedList(this.getList("trusted.servers"));
            if (servers.size() > 100) {
                while (servers.size() > 50) {
                    servers.removeFirst();
                }
                this.setList("trusted.servers", servers);
            }
            LinkedHashSet profiles = new LinkedHashSet(this.getList("connection.profiles"));
            Iterator i = this.data.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry next = (Map.Entry) i.next();
                if (!next.getKey().toString().startsWith("connection.profiles.")) continue;
                String host = next.getKey().toString().substring("connection.profiles.".length());
                if (profiles.contains(host.substring(0, host.lastIndexOf(".")))) continue;
                i.remove();
            }
        } catch (Exception ex) {
            MudgeSanity.logException("scrub preferences", ex, false);
        }
    }

    public void save() {
        this.scrub();
        File file = this.myFile();
        try {
            FileOutputStream out = new FileOutputStream(file);
            this.data.store(out, "Cobalt Strike (Aggressor) Configuration");
            out.close();
        } catch (IOException ioex) {
            MudgeSanity.logException("Save Preferences: " + file, ioex, false);
        }
    }

    public String getString(String key, String defaultv) {
        return this.data.getProperty(key, defaultv);
    }

    public boolean isSet(String key, boolean defaultv) {
        return "true".equals(this.getString(key, defaultv + ""));
    }

    public long getLongNumber(String key, long defaultv) {
        return CommonUtils.toLongNumber(this.getString(key, defaultv + ""), defaultv);
    }

    public int getRandomPort(String key, String defaultr) {
        String temp = this.getString(key, defaultr);
        if ("".equals(temp)) {
            temp = defaultr;
        }
        RangeList range = new RangeList(temp);
        return range.random();
    }

    public Color getColor(String key, String defaultv) {
        return Color.decode(this.getString(key, defaultv));
    }

    public Font getFont(String key, String defaultv) {
        return Font.decode(this.getString(key, defaultv));
    }

    public List getList(String key) {
        String val = this.getString(key, "");
        if ("".equals(val)) {
            return new LinkedList();
        }
        return CommonUtils.toList(val.split("!!"));
    }

    public void appendList(String key, String value) {
        List mylist = this.getList(key);
        mylist.add(value);
        this.setList(key, new LinkedList(new LinkedHashSet(mylist)));
    }

    public void setList(String key, List value) {
        value = new LinkedList(value);
        Iterator i = value.iterator();
        while (i.hasNext()) {
            String temp = (String) i.next();
            if (temp != null && !"".equals(temp)) continue;
            i.remove();
        }
        this.set(key, CommonUtils.join(value, "!!"));
    }

    public void set(String key, String value) {
        this.data.setProperty(key, value);
    }

    public void update(Map options) {
        for (Object o : options.entrySet()) {
            Map.Entry temp = (Map.Entry) o;
            String key = (String) temp.getKey();
            String value = (String) temp.getValue();
            this.data.setProperty(key, value);
        }
        this.save();
    }

    public Map copy() {
        return new HashMap<>(this.data);
    }

    public static Prefs getPreferences() {
        prefs.load();
        return prefs;
    }
}

