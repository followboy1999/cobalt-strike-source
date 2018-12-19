package common;

import beacon.Settings;
import dialog.DialogUtils;

import java.util.HashMap;
import java.util.Map;

public class ProxyServer {
    public static final int PROXY_MANUAL = 0;
    public static final int PROXY_DIRECT = 1;
    public static final int PROXY_PRECONFIG = 2;
    public static final int PROXY_MANUAL_CREDS = 4;
    public String username = null;
    public String password = null;
    public String phost = "";
    public int pport = 8080;
    public String ptype = "";
    public int means = 2;

    public boolean hasCredentials() {
        return this.username != null && this.password != null && this.username.length() > 0 && this.password.length() > 0;
    }

    public boolean hasHostAndPort() {
        return this.phost != null && this.pport > 0 && this.phost.length() > 0;
    }

    public String toString() {
        if (this.means == 1) {
            return "*direct*";
        }
        if (this.means == 2) {
            return "";
        }
        if (this.hasHostAndPort()) {
            StringBuilder result = new StringBuilder();
            result.append(this.ptype);
            result.append("://");
            if (this.hasCredentials()) {
                result.append(CommonUtils.URLEncode(this.username));
                result.append(":");
                result.append(CommonUtils.URLEncode(this.password));
                result.append("@");
            }
            result.append(this.phost);
            result.append(":");
            result.append(this.pport);
            return result.toString();
        }
        return "";
    }

    public static ProxyServer resolve(Map options) {
        ProxyServer result = new ProxyServer();
        if (options.size() == 0) {
            result.means = 2;
        } else if (DialogUtils.bool(options, "pdirect")) {
            result.means = 1;
        } else {
            result.means = 0;
            result.ptype = DialogUtils.string(options, "ptype");
            result.phost = DialogUtils.string(options, "phost");
            result.pport = CommonUtils.toNumber(DialogUtils.string(options, "pport"), 8080);
            if (options.containsKey("puser") && options.containsKey("ppass")) {
                result.username = DialogUtils.string(options, "puser");
                result.password = DialogUtils.string(options, "ppass");
            }
        }
        return result;
    }

    public Map toMap() {
        HashMap<String, String> result = new HashMap<>();
        if (this.means == 1) {
            result.put("pdirect", "true");
            return result;
        }
        if (this.means == 2) {
            return new HashMap();
        }
        if (this.username != null && this.password != null) {
            result.put("puser", this.username);
            result.put("ppass", this.password);
        }
        result.put("phost", this.phost);
        result.put("pport", this.pport + "");
        result.put("ptype", this.ptype);
        return result;
    }

    public static ProxyServer parse(String text) {
        ProxyServer result = new ProxyServer();
        RegexParser parser = new RegexParser(text);
        if ("".equals(text)) {
            result.means = 2;
            return result;
        }
        if ("*direct*".equals(text)) {
            result.means = 1;
            return result;
        }
        if (parser.matches("(.*?)://(.*?):(.*?)@(.*?):(.*?)")) {
            result.ptype = parser.group(1);
            result.username = CommonUtils.URLDecode(parser.group(2));
            result.password = CommonUtils.URLDecode(parser.group(3));
            result.phost = parser.group(4);
            result.pport = CommonUtils.toNumber(parser.group(5), 5555);
            result.means = 0;
            return result;
        }
        if (parser.matches("(.*?)://(.*?):(.*?)")) {
            result.ptype = parser.group(1);
            result.phost = parser.group(2);
            result.pport = CommonUtils.toNumber(parser.group(3), 5555);
            result.means = 0;
            return result;
        }
        result.means = 2;
        return result;
    }

    public void setup(Settings s) {
        if (this.means == 1) {
            s.addShort(35, this.means);
        } else if (this.means == 2) {
            s.addShort(35, this.means);
        } else if (this.hasHostAndPort()) {
            StringBuilder result = new StringBuilder();
            if ("socks".equals(this.ptype)) {
                result.append("socks=");
            }
            if ("http".equals(this.ptype)) {
                result.append("http://");
            }
            if ("https".equals(this.ptype)) {
                result.append("https://");
            }
            result.append(this.phost);
            result.append(":");
            result.append(this.pport);
            s.addString(32, result.toString(), 128);
            if (this.hasCredentials()) {
                s.addShort(35, 4);
                s.addString(33, this.username, 64);
                s.addString(34, this.password, 64);
            } else {
                s.addShort(35, 0);
            }
        } else {
            AssertUtils.TestFail("means not known: " + this.means);
        }
    }
}

