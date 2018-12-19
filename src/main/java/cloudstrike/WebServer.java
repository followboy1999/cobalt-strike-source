package cloudstrike;

import common.CommonUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServer extends NanoHTTPD {
    protected HashMap<String, WebService> hooks = new HashMap<>();
    protected HashMap<String, WebService> hooksSecondary = new HashMap<>();
    protected HashMap<String,Boolean> always = new HashMap<>();
    protected HashMap<String, String> hosts = new HashMap<>();
    protected LinkedList<WebListener> weblisteners = new LinkedList<>();

    public void associate(String uri, String host) {
        this.hosts.put(uri, host);
    }

    public void addWebListener(WebListener l) {
        this.weblisteners.add(l);
    }

    public List sites() {
        LinkedList<Map> temp = new LinkedList<>();
        for (Map.Entry<String, WebService> entry : this.hooks.entrySet()) {
            String uri = entry.getKey() + "";
            String desc = entry.getValue() + "";
            String type = entry.getValue().getType();
            String host = this.hosts.get(uri);
            HashMap<String, String> result = new HashMap<>();
            result.put("URI", uri);
            result.put("Description", desc);
            result.put("Type", type);
            result.put("Host", host);
            if (this.isSSL()) {
                result.put("Proto", "https://");
            } else {
                result.put("Proto", "http://");
            }
            temp.add(result);
        }
        return temp;
    }

    public void register(String uri, WebService service) {
        if (this.hooks.containsKey(uri)) {
            throw new RuntimeException("URI " + uri + " hosts: " + this.get(uri).getType());
        }
        this.always.remove(uri);
        this.hooks.put(uri, service);
    }

    public void registerSecondary(String uri, WebService service) {
        this.always.remove(uri);
        this.hooksSecondary.put(uri, service);
    }

    public void setSpecialPostURI(String uri) {
        this.always.put(uri, Boolean.TRUE);
    }

    @Override
    public boolean alwaysRaw(String uri) {
        if (this.always.containsKey(uri)) {
            return true;
        }
        return this.always.keySet().stream().anyMatch(uri::startsWith);
    }

    public WebService get(String uri) {
        return this.hooks.get(uri);
    }

    public boolean deregister(String uri) {
        this.hooks.remove(uri);
        this.always.remove(uri);
        this.hosts.remove(uri);
        if (this.hooks.size() == 0) {
            this.stop();
            return true;
        }
        return false;
    }

    protected void fireWebListener(String uri, String method, Properties header, Properties param, String desc, boolean primary, String response, long size) {
        this.weblisteners.forEach(weblistener -> weblistener.receivedClient(uri, method, header, param, desc, primary, response, size));
    }

    protected Response processResponse(String uri, String method, Properties header, Properties param, boolean primary, WebService service, Response r) {
        String desc = service == null ? null : service.getType() + " " + service.toString();
        String resp = r.status;
        long size = r.size;
        if (service == null || !service.suppressEvent(uri)) {
            this.fireWebListener(uri, method, header, param, desc, primary, resp, size);
        }
        return r;
    }

    public static long checksum8(String text) {
        return CommonUtils.checksum8(text);
    }

    public static boolean isStager(String uri) {
        return WebServer.checksum8(uri) == 92L;
    }

    public static boolean isStagerX64(String uri) {
        return WebServer.checksum8(uri) == 93L && uri.matches("/[A-Za-z0-9]{4}");
    }

    public static boolean isStagerStrict(String uri) {
        return WebServer.isStager(uri) && uri.length() == 5;
    }

    public static boolean isStagerX64Strict(String uri) {
        return WebServer.isStagerX64(uri) && uri.length() == 5;
    }

    public Response handleRanges(String method, Properties header, Response original) {
        if ((header.containsKey("Range")) && ("GET".equals(method)) && (original.size > 0L) && (original.data != null) && ("200 OK".equals(original.status))) {
            Pattern p = Pattern.compile("bytes=(\\d+)-(\\d+)");
            Matcher m = p.matcher((String) header.get("Range"));
            if (m.matches()) {
                int start = Integer.parseInt(m.group(1));
                int end = Integer.parseInt(m.group(2)) + 1;
                try {
                    if ((start < end) && (end <= original.size)) {
                        byte[] rdata = new byte[end - start];
                        original.data.skip(start);
                        int r = original.data.read(rdata, 0, rdata.length);
                        if (r != rdata.length) {
                            throw new RuntimeException("Read " + r + " bytes instead of expected " + rdata.length);
                        }
                        original.addHeader("Content-Range", "bytes " + start + "-" + m.group(2) + "/" + original.size);
                        original.addHeader("Content-Length", rdata.length + "");
                        original.size = rdata.length;
                        original.data = new ByteArrayInputStream(rdata);
                        original.status = "206 Partial Content";
                        return original;
                    }
                } catch (Exception e) {
                    logException("Range handling failed: " + header.get("Range") + "; " + start + ", " + end + "; size = " + original.size, e, false);
                }

                Response bust = new Response("416 Range Not Satisfiable", "text/plain", "Range Not Satisfiable");
                bust.addHeader("Content-Range", "bytes */" + original.size);
                return bust;
            }
        }

        return original;
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties param) {
        return this.handleRanges(method, header, this._serve(uri, method, header, param));
    }

    public Response _serve(String uri, String method, Properties header, Properties param) {
        WebService service;
        String useragent = (header.getProperty("User-Agent") + "").toLowerCase();
        if (useragent.startsWith("lynx") || useragent.startsWith("curl") || useragent.startsWith("wget")) {
            return this.processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }
        if (method.equals("OPTIONS")) {
            Response r = this.processResponse(uri, method, header, param, false, null, new Response("200 OK", "text/html", ""));
            r.addHeader("Allow", "OPTIONS,GET,HEAD,POST");
            return r;
        }
        if (this.hooks.containsKey(uri)) {
            WebService service2 = this.hooks.get(uri);
            return this.processResponse(uri, method, header, param, true, service2, service2.serve(uri, method, header, param));
        }
        if (this.hooksSecondary.containsKey(uri)) {
            WebService service3 = this.hooksSecondary.get(uri);
            return this.processResponse(uri, method, header, param, false, service3, service3.serve(uri, method, header, param));
        }
        if (this.hooks.containsKey(uri + "/")) {
            WebService service4 = this.hooks.get(uri + "/");
            return this.processResponse(uri + "/", method, header, param, true, service4, service4.serve(uri, method, header, param));
        }
        if (uri.startsWith("http://")) {
            WebService service5 = this.hooks.get("proxy");
            if (service5 != null) {
                return this.processResponse(uri, method, header, param, true, service5, service5.serve(uri, method, header, param));
            }
            return this.processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }
        if (WebServer.isStagerX64Strict(uri) && this.hooks.containsKey("stager64")) {
            WebService service6 = this.hooks.get("stager64");
            return this.processResponse(uri + "/", method, header, param, true, service6, service6.serve(uri, method, header, param));
        }
        if (WebServer.isStagerStrict(uri) && this.hooks.containsKey("stager")) {
            WebService service7 = this.hooks.get("stager");
            return this.processResponse(uri + "/", method, header, param, true, service7, service7.serve(uri, method, header, param));
        }
        for (Map.Entry<String, WebService> e : this.hooksSecondary.entrySet()) {
            WebService svc = e.getValue();
            String hook = e.getKey();
            if (!uri.startsWith(hook) || !svc.isFuzzy()) continue;
            return this.processResponse(uri, method, header, param, false, svc, svc.serve(uri.substring(hook.length()), method, header, param));
        }
        if (WebServer.isStagerX64(uri) && this.hooks.containsKey("stager64")) {
            service = this.hooks.get("stager64");
            return this.processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (WebServer.isStagerX64(uri)) {
            WebServer.print_warn("URI Matches staging (x64) URL, but there is no stager bound...: " + uri);
            return this.processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }
        if (WebServer.isStager(uri) && this.hooks.containsKey("stager")) {
            service = this.hooks.get("stager");
            return this.processResponse(uri + "/", method, header, param, true, service, service.serve(uri, method, header, param));
        }
        if (WebServer.isStager(uri)) {
            WebServer.print_warn("URI Matches staging (x86) URL, but there is no stager bound...: " + uri);
            return this.processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
        }
        return this.processResponse(uri, method, header, param, false, null, new Response("404 Not Found", "text/plain", ""));
    }

    public WebServer(int port) throws IOException {
        super(port);
    }

    public WebServer(int port, boolean ssl, InputStream keystore, String password) throws IOException {
        super(port, ssl, keystore, password);
    }

    public static void main(String[] args) {
        try {
            new WebServer(80);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface WebListener {
        void receivedClient(String var1, String var2, Properties var3, Properties var4, String var5, boolean var6, String var7, long var8);
    }

}

