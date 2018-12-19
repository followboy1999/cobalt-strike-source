package aggressor.dialogs;

import aggressor.Aggressor;
import aggressor.AggressorClient;
import aggressor.MultiFrame;
import aggressor.Prefs;
import common.*;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogs;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Connect implements DialogListener,
        Callback,
        ArmitageTrustListener {
    protected MultiFrame window;
    protected TeamQueue tqueue = null;
    protected String desc = "";
    protected Map options = null;

    public Connect(MultiFrame window) {
        this.window = window;
    }

    @Override
    public boolean trust(String fingerprint) {
        HashSet set = new HashSet(Prefs.getPreferences().getList("trusted.servers"));
        if (set.contains(fingerprint)) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(null, "The team server's fingerprint is:\n\n<html><body><b>" + fingerprint + "</b></body></html>\n\nDoes this match the fingerprint shown when the team server started?", "VerifyFingerprint", JOptionPane.YES_NO_OPTION);
        if (result == 0) {
            Prefs.getPreferences().appendList("trusted.servers", fingerprint);
            Prefs.getPreferences().save();
            return true;
        }
        return false;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        String user = options.get("user") + "";
        String host = options.get("host") + "";
        String port = options.get("port") + "";
        String pass = options.get("pass") + "";
        Prefs.getPreferences().appendList("connection.profiles", host);
        Prefs.getPreferences().set("connection.profiles." + host + ".user", user);
        Prefs.getPreferences().set("connection.profiles." + host + ".port", port);
        Prefs.getPreferences().set("connection.profiles." + host + ".password", pass);
        Prefs.getPreferences().save();
        this.desc = user + "@" + host;
        try {
            SecureSocket client = new SecureSocket(host, Integer.parseInt(port), this);
            client.authenticate(pass);
            TeamSocket tclient = new TeamSocket(client.getSocket());
            this.tqueue = new TeamQueue(tclient);
            this.tqueue.call("aggressor.authenticate", CommonUtils.args(user, pass, Aggressor.VERSION), this);
        } catch (Exception ioex) {
            if ("127.0.0.1".equals(host) && "Connection refused".equals(ioex.getMessage())) {
                MudgeSanity.logException("client connect", ioex, true);
                SafeDialogs.askYesNo("Connection refused\n\nA Cobalt Strike team server is not available on\nthe specified host and port. You must start a\nCobalt Strike team server first. Would you like\nto review the documentation on how to do this?", "Connection Error", foo -> DialogUtils.gotoURL("https://www.cobaltstrike.com/help-start-cobaltstrike").actionPerformed(null));
            }
            MudgeSanity.logException("client connect", ioex, true);
            DialogUtils.showError(ioex.getMessage());
        }
    }

    @Override
    public void result(String method, Object data) {
        if ("aggressor.authenticate".equals(method)) {
            String result = data + "";
            if (result.equals("SUCCESS")) {
                this.tqueue.call("aggressor.metadata", CommonUtils.args(System.currentTimeMillis()), this);
            } else {
                DialogUtils.showError(result);
                this.tqueue.close();
            }
        } else if ("aggressor.metadata".equals(method)) {
            final AggressorClient client = new AggressorClient(this.window, this.tqueue, (Map) data, this.options);
            CommonUtils.runSafe(() -> {
                Connect.this.window.addButton(Connect.this.desc, client);
                client.showTime();
            });
        }
    }

    public JComponent getContent(JFrame dialogd, String user, String pass, String host, String port) {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        DialogManager controller = new DialogManager(dialogd);
        controller.addDialogListener(this);
        controller.set("user", user);
        controller.set("pass", pass);
        controller.set("host", host);
        controller.set("port", port);
        controller.text("host", "Host:", 20);
        controller.text("port", "Port:", 10);
        controller.text("user", "User:", 20);
        controller.password("pass", "Password:", 20);
        JButton ok = controller.action("Connect");
        JButton help = controller.help("https://www.cobaltstrike.com/help-setup-collaboration");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        return dialog;
    }

}

