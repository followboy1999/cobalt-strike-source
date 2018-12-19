package server;

import cloudstrike.WebServer;
import common.*;
import endpoint.*;
import icmp.Server;
import tap.EncryptedTap;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VPN implements ServerHook,
        Do {
    protected Resources resources;
    protected Map vpn = new HashMap();
    protected Map taps = new HashMap();
    protected Map srv = new HashMap();
    protected boolean loaded = false;
    protected Server iserver = null;

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("cloudstrike.start_tap", this);
        calls.put("cloudstrike.stop_tap", this);
        calls.put("cloudstrike.set_tap_hwaddr", this);
    }

    public VPN(Resources r) {
        this.resources = r;
    }

    public boolean hasVPN(String intf) {
        synchronized (this) {
            return this.vpn.containsKey(intf);
        }
    }

    public EncryptedTap getTap(String intf) {
        synchronized (this) {
            return (EncryptedTap) this.taps.get(intf);
        }
    }

    public Base getServer(String intf) {
        synchronized (this) {
            return (Base) this.srv.get(intf);
        }
    }

    public List buildVPNModel() {
        synchronized (this) {
            LinkedList mymodel = new LinkedList();
            for (Object o : this.vpn.values()) {
                Map entry = (Map) o;
                String intf = (String) entry.get("interface");
                EncryptedTap tap = this.getTap(intf);
                Base server = this.getServer(intf);
                if (tap.isActive()) {
                    entry.put("client", tap.getRemoteHost());
                    entry.put("tx", server.getTransmittedBytes());
                    entry.put("rx", server.getReceivedBytes());
                }
                mymodel.add(new HashMap(entry));
            }
            return mymodel;
        }
    }

    @Override
    public boolean moment(String message) {
        this.resources.broadcast("interfaces", this.buildVPNModel());
        return true;
    }

    public void report(String intf, String hwaddr, byte[] secret, String channel, int port, String client, String hook) {
        synchronized (this) {
            HashMap<String, Object> temp = new HashMap<>();
            temp.put("interface", intf);
            temp.put("mac", hwaddr);
            temp.put("secret", secret);
            temp.put("channel", channel);
            temp.put("port", port);
            temp.put("client", client);
            temp.put("useragent", ServerUtils.randua(this.resources));
            temp.put("hook", hook);
            this.vpn.put(intf, temp);
        }
    }

    public boolean loadTapLibrary() {
        synchronized (this) {
            if (this.loaded) {
                return true;
            }
            try {
                if (CommonUtils.is64bit()) {
                    System.load(CommonUtils.dropFile("libtapmanager64.so", "cobalt_tapmanager", ".so"));
                } else {
                    System.load(CommonUtils.dropFile("libtapmanager.so", "cobalt_tapmanager", ".so"));
                }
                this.loaded = true;
                Timers.getTimers().every(1000L, "vpn", this);
                return true;
            } catch (Exception ex) {
                MudgeSanity.logException("loadTapLibrary", ex, false);
            }
        }
        return false;
    }

    public Server loadICMPLibrary() {
        synchronized (this) {
            if (this.iserver != null) {
                return this.iserver;
            }
            try {
                if (CommonUtils.is64bit()) {
                    System.load(CommonUtils.dropFile("libicmp64.so", "icmp", ".so"));
                } else {
                    System.load(CommonUtils.dropFile("libicmp.so", "icmp", ".so"));
                }
                this.iserver = new Server();
                return this.iserver;
            } catch (Exception ex) {
                MudgeSanity.logException("loadICMPLibrary", ex, false);
            }
        }
        return null;
    }

    public void stop_tap(ManageUser client, Request r) {
        synchronized (this) {
            String intf = (String) r.arg(0);
            if (this.srv.containsKey(intf)) {
                Base server = (Base) this.srv.get(intf);
                server.quit();
            }
            this.taps.remove(intf);
            this.srv.remove(intf);
            this.vpn.remove(intf);
        }
    }

    public void set_tap_address(ManageUser client, Request r) {
        String intf = (String) r.arg(0);
        String mac = (String) r.arg(1);
        synchronized (this) {
            if (this.hasVPN(intf)) {
                EncryptedTap tap = (EncryptedTap) this.taps.get(intf);
                tap.setHWAddress(this.macToByte(mac));
                Map entry = (Map) this.vpn.get(intf);
                entry.put("mac", mac);
            }
        }
    }

    public byte[] macToByte(String mac) {
        String[] src = mac.split(":");
        byte[] dst = new byte[src.length];
        for (int x = 0; x < src.length; ++x) {
            dst[x] = (byte) Integer.parseInt(src[x], 16);
        }
        return dst;
    }

    public void start_tap(ManageUser client, Request r, String intf, String hwaddr, int port, String channel) {
        if (this.hasVPN(intf)) {
            client.writeNow(r.reply(intf + " is already defined"));
            return;
        }
        if (!RegexParser.isMatch(hwaddr, "[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}:[a-fA-F0-9]{2}")) {
            client.writeNow(r.reply("invalid mac address"));
            return;
        }
        if (!this.loadTapLibrary()) {
            client.writeNow(r.reply("could not load tap library"));
            return;
        }
        try {
            new SecureRandom();
            byte[] secret = SecureRandom.getSeed(16);
            EncryptedTap tap = new EncryptedTap(intf, secret);
            String hook = "";
            if ("UDP".equals(channel)) {
                UDP server = new UDP(tap, port);
                this.srv.put(intf, server);
            } else if ("TCP (Bind)".equals(channel)) {
                TCP server = new TCP(tap, port, false);
                this.srv.put(intf, server);
            } else if ("TCP (Reverse)".equals(channel)) {
                TCP server = new TCP(tap, port, true);
                this.srv.put(intf, server);
            } else if ("HTTP".equals(channel)) {
                WebCalls web = ServerUtils.getWebCalls(this.resources);
                WebServer server = web.getWebServer(port);
                HTTP handler = new HTTP(tap);
                hook = "/" + intf + ".json";
                handler.setup(server, hook);
                this.srv.put(intf, handler);
            } else if ("ICMP".equals(channel)) {
                this.loadICMPLibrary();
                ICMP handler = new ICMP(tap);
                hook = CommonUtils.ID().substring(0, 4);
                this.iserver.addIcmpListener(hook, handler);
                this.srv.put(intf, handler);
            }
            this.report(intf, hwaddr, secret, channel, port, "not connected", hook);
            this.taps.put(intf, tap);
        } catch (Exception ex) {
            MudgeSanity.logException("start_tap", ex, false);
            client.writeNow(r.reply(ex.getMessage()));
        }
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("cloudstrike.start_tap", 4)) {
            this.start_tap(client, r, (String) r.arg(0), (String) r.arg(1), Integer.parseInt((String) r.arg(2)), (String) r.arg(3));
        } else if (r.is("cloudstrike.stop_tap", 1)) {
            this.stop_tap(client, r);
        } else if (r.is("cloudstrike.set_tap_hwaddr", 2)) {
            this.set_tap_address(client, r);
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }
}

