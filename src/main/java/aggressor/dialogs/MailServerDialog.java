package aggressor.dialogs;

import common.AObject;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import phish.MailServer;
import phish.PhishingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class MailServerDialog
        extends AObject implements DialogListener {
    protected JFrame dialog = null;
    protected SafeDialogCallback callback;
    protected String oldv;

    public MailServerDialog(String oldvalue, SafeDialogCallback callback) {
        this.callback = callback;
        this.oldv = oldvalue;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String user = DialogUtils.string(options, "USERNAME");
        String pass = DialogUtils.string(options, "PASSWORD");
        int delay = DialogUtils.number(options, "Delay");
        String conn = DialogUtils.string(options, "connect");
        String host = DialogUtils.string(options, "LHOST");
        int port = DialogUtils.number(options, "LPORT");
        StringBuilder result = new StringBuilder();
        if (!"".equals(user) && !"".equals(pass)) {
            result.append(user);
            result.append(":");
            result.append(pass);
            result.append("@");
        }
        result.append(host);
        if (port != 25) {
            result.append(":");
            result.append(port);
        }
        if ("SSL".equals(conn)) {
            result.append("-ssl");
        } else if ("STARTTLS".equals(conn)) {
            result.append("-starttls");
        }
        if (delay > 0) {
            result.append(",");
            result.append(delay);
        }
        this.callback.dialogResult(result.toString());
    }

    public void parseOld(DialogManager controller) {
        MailServer old = PhishingUtils.parseServerString(this.oldv);
        if (old.username != null) {
            controller.set("USERNAME", old.username);
        }
        if (old.password != null) {
            controller.set("PASSWORD", old.password);
        }
        controller.set("Delay", old.delay + "");
        if (old.lhost != null) {
            controller.set("LHOST", old.lhost);
        }
        controller.set("LPORT", old.lport + "");
        if (old.starttls) {
            controller.set("connect", "STARTTLS");
        } else if (old.ssl) {
            controller.set("connect", "SSL");
        } else {
            controller.set("connect", "Plain");
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Mail Server", 320, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        this.parseOld(controller);
        controller.text("LHOST", "SMTP Host:", 20);
        controller.text("LPORT", "SMTP Port:", 20);
        controller.text("USERNAME", "Username:", 20);
        controller.text("PASSWORD", "Password:", 20);
        controller.text("Delay", "Random Delay:", 20);
        controller.combobox("connect", "Connection:", CommonUtils.toArray("Plain, SSL, STARTTLS"));
        JButton set = controller.action("Set");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(set), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}

