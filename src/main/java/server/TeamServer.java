package server;

import c2profile.Loader;
import c2profile.Profile;
import common.*;
import dns.QuickSecurity;
import ssl.SecureServerSocket;

import java.util.HashMap;
import java.util.Iterator;

public class TeamServer {
    protected int port;
    protected String host;
    protected Resources resources;
    protected HashMap<String, Object> calls;
    protected Profile c2profile;
    protected String pass;
    protected Authorization auth;
    private static String host_help;

    static {
        host_help = "It's best if your targets can reach your team server via this IP address. It's OK if this IP address is a redirector.\n\nWhy does this matter?\n\nCobalt Strike uses this IP address as a default throughout its workflows. Cobalt Strike's DNS Beacon also uses this IP address for its HTTP channel. The Covert VPN feature uses this IP too. If your target can't reach your team server via this IP, it's possible some CS features may not work as expected.";
    }

    public TeamServer(String host, int port, String pass, Profile profile, Authorization auth) {
        this.host = host;
        this.port = port;
        this.pass = pass;
        this.c2profile = profile;
        this.auth = auth;
        calls = new HashMap<>();
    }

    public void go() {
        try {
            new ProfileEdits(this.c2profile);
            this.c2profile.addParameter(".watermark", this.auth.getWatermark());
            this.resources = new Resources(this.calls);
            this.resources.put("c2profile", this.c2profile);
            this.resources.put("localip", this.host);
            this.resources.put("password", this.pass);
            new TestCall().register(this.calls);
            WebCalls web = new WebCalls(this.resources);
            web.register(this.calls);
            this.resources.put("webcalls", web);
            new Listeners(this.resources).register(this.calls);
            new Beacons(this.resources).register(this.calls);
            new Phisher(this.resources).register(this.calls);
            new VPN(this.resources).register(this.calls);
            new BrowserPivotCalls(this.resources).register(this.calls);
            new DownloadCalls(this.resources).register(this.calls);
            Iterator i = Keys.getDataModelIterator();
            while (i.hasNext()) {
                new DataCalls(this.resources, (String) i.next()).register(this.calls);
            }
            if (!ServerUtils.hasPublicStage(this.resources)) {
                CommonUtils.print_warn("Woah! Your profile disables hosted payload stages. Payload staging won't work.");
            }
            SecureServerSocket server = new SecureServerSocket(this.port);
            CommonUtils.print_good("Team server is up on " + this.port);
            CommonUtils.print_info("SHA256 hash of SSL cert is: " + server.fingerprint());
            this.resources.call("listeners.go");
            while (true) {
                server.acceptAndAuthenticate(this.pass, client -> {
                    try {
                        client.setSoTimeout(0);
                        TeamSocket plebe = new TeamSocket(client);
                        new Thread(new ManageUser(plebe, TeamServer.this.resources, TeamServer.this.calls), "Manage: unauth'd user").start();
                    } catch (Exception ex) {
                        MudgeSanity.logException("Start client thread", ex, false);
                    }
                });
            }
        } catch (Exception ex) {
            MudgeSanity.logException("team server startup", ex, false);
            return;
        }
    }

    public static void main(String[] args) {
        int DEFAULT_PORT = CommonUtils.toNumber(System.getProperty("cobaltstrike.server_port", "50050"), 50050);
        if (!AssertUtils.TestPort(DEFAULT_PORT)) {
            System.exit(0);
        }
        Requirements.checkConsole();
        Authorization auth = new Authorization();
        License.checkLicenseConsole(auth);
        MudgeSanity.systemDetail("scheme", QuickSecurity.getCryptoScheme() + "");
        if (args.length == 0 || args.length == 1 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            CommonUtils.print_info("./teamserver <host> <password> [/path/to/c2.profile] [YYYY-MM-DD]\n\n\t<host> is the (default) IP address of this Cobalt Strike team server\n\t<password> is the shared password to connect to this server\n\t[/path/to/c2.profile] is your Malleable C2 profile\n\t[YYYY-MM-DD] is a kill date for Beacon payloads run from this server\n");
        } else if (args.length != 2 && args.length != 3 && args.length != 4) {
            CommonUtils.print_error("Missing arguments to start team server\n\t./teamserver <host> <password> [/path/to/c2.profile] [YYYY-MM-DD]");
        } else if (!CommonUtils.isIP(args[0])) {
            CommonUtils.print_error("The team server <host> must be an IP address. " + host_help);
        } else if ("127.0.0.1".equals(args[0])) {
            CommonUtils.print_error("Don't use 127.0.0.1 for the team server <host>. " + host_help);
        } else if ("0.0.0.0".equals(args[0])) {
            CommonUtils.print_error("Don't use 0.0.0.0 for the team server <host>. " + host_help);
        } else if (args.length == 2) {
            MudgeSanity.systemDetail("c2Profile", "default");
            TeamServer server = new TeamServer(args[0], DEFAULT_PORT, args[1], Loader.LoadDefaultProfile(), auth);
            server.go();
        } else if (args.length == 3 || args.length == 4) {
            MudgeSanity.systemDetail("c2Profile", args[2]);
            Profile profile = Loader.LoadProfile(args[2]);
            if (profile == null) {
                CommonUtils.print_error("exiting because of errors in " + args[2] + ". Use ./c2lint to check the file");
                System.exit(0);
            }
            CommonUtils.print_good("I see you're into threat replication. " + args[2] + " loaded.");
            if (args.length == 4) {
                long killdate = CommonUtils.parseDate(args[3], "yyyy-MM-dd");
                if (killdate < System.currentTimeMillis()) {
                    CommonUtils.print_error("Beacon kill date " + args[3] + " is in the past!");
                    System.exit(0);
                } else if (killdate > 0L) {
                    CommonUtils.print_good("Beacon kill date is: " + args[3] + "!");
                    profile.addParameter(".killdate", args[3]);
                } else {
                    CommonUtils.print_error("Invalid kill date: '" + args[3] + "' (format is YYYY-MM-DD)");
                    System.exit(0);
                }
                MudgeSanity.systemDetail("kill date", args[3]);
            } else {
                MudgeSanity.systemDetail("kill date", "none");
            }
            TeamServer server = new TeamServer(args[0], DEFAULT_PORT, args[1], profile, auth);
            server.go();
        }
    }

}

