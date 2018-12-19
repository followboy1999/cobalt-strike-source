package aggressor.headless;

import aggressor.Aggressor;
import aggressor.MultiFrame;
import common.*;
import sleep.parser.ParserConfig;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

import java.util.Map;

public class Start implements Callback,
        ArmitageTrustListener {
    protected MultiFrame window;
    protected TeamQueue tqueue = null;
    protected String desc = "";
    protected String script = "";

    public Start(MultiFrame window) {
        this.window = window;
    }

    public static void main(String[] args) {
        ParserConfig.installEscapeConstant('c', "\u0003");
        ParserConfig.installEscapeConstant('U', "\u001f");
        ParserConfig.installEscapeConstant('o', "\u000f");
        License.checkLicenseConsole(new Authorization());
        if (args.length == 5) {
            String host = args[0];
            int port = CommonUtils.toNumber(args[1], 50050);
            String user = args[2];
            String pass = args[3];
            String script = args[4];
            new Start(null).go(host, port, user, pass, script);
        } else if (args.length == 4) {
            String host = args[0];
            int port = CommonUtils.toNumber(args[1], 50050);
            String user = args[2];
            String pass = args[3];
            new Start(null).go(host, port, user, pass, null);
        } else {
            System.out.println("Welcome to the Cobalt Strike (Headless) Client. Version " + Aggressor.VERSION + "\nCopyright 2015, Strategic Cyber LLC\n\nQuick help:\n\n\t./agscript [host] [port] [user] [pass]\n\t\tConnect to a team server and start the Aggressor Script console\n\n\t./agscript [host] [port] [user] [pass] </path/to/file.cna>\n\t\tConnect to a team server and execute the specified script");
            System.exit(0);
        }
    }

    @Override
    public boolean trust(String fingerprint) {
        return true;
    }

    public void go(String host, int port, String user, String pass, String script) {
        this.script = script;
        try {
            SecureSocket client = new SecureSocket(host, port, this);
            client.authenticate(pass);
            TeamSocket tclient = new TeamSocket(client.getSocket());
            this.tqueue = new TeamQueue(tclient);
            this.tqueue.call("aggressor.authenticate", CommonUtils.args(user, pass, Aggressor.VERSION), this);
        } catch (Exception ioex) {
            MudgeSanity.logException("client connect", ioex, true);
        }
    }

    @Override
    public void result(String method, Object data) {
        if ("aggressor.authenticate".equals(method)) {
            String result = data + "";
            if (result.equals("SUCCESS")) {
                this.tqueue.call("aggressor.metadata", CommonUtils.args(System.currentTimeMillis()), this);
            } else {
                CommonUtils.print_error(result);
                this.tqueue.close();
                System.exit(0);
            }
        } else if ("aggressor.metadata".equals(method)) {
            new HeadlessClient(this.window, this.tqueue, (Map) data, this.script);
        }
    }
}

