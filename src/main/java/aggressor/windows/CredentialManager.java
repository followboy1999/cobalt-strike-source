package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Credentials;
import aggressor.dialogs.CredentialDialog;
import common.AObject;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Map;

public class CredentialManager
        extends AObject implements ActionListener {
    protected AggressorClient client;
    protected Credentials browser;
    protected ActivityPanel dialog;

    public CredentialManager(AggressorClient client) {
        this.client = client;
        this.browser = new Credentials(client);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Add".equals(ev.getActionCommand())) {
            new CredentialDialog(this.client).show();
        } else if ("Edit".equals(ev.getActionCommand())) {
            Map[] options = this.browser.getSelectedRows();
            for (Map option : options) {
                new CredentialDialog(this.client, option).show();
            }
        } else if ("Remove".equals(ev.getActionCommand())) {
            Map[] options = this.browser.getSelectedRows();
            for (Map option : options) {
                this.client.getConnection().call("credentials.remove", CommonUtils.args(CommonUtils.CredKey(option)));
            }
            this.client.getConnection().call("credentials.push");
        } else if ("Copy".equals(ev.getActionCommand())) {
            StringBuilder creds = new StringBuilder();
            Map[] options = this.browser.getSelectedRows();
            for (Map option : options) {
                if (creds.length() > 0) {
                    creds.append("\n");
                }
                if (!"".equals(option.get("realm"))) {
                    creds.append(option.get("realm"));
                    creds.append("\\");
                }
                creds.append(option.get("user"));
                creds.append(" ");
                creds.append(option.get("password"));
            }
            DialogUtils.addToClipboard(creds.toString());
        } else if ("Export".equals(ev.getActionCommand())) {
            final StringBuilder creds = new StringBuilder();
            creds.append("# Cobalt Strike Credential Export\n");
            creds.append("# ").append(CommonUtils.formatTime(System.currentTimeMillis())).append("\n\n");
            for (Map option : (LinkedList<Map>) this.client.getData().getListSafe("credentials")) {
                if (!"".equals(option.get("realm"))) {
                    creds.append(option.get("realm"));
                    creds.append("\\");
                }
                String user = option.get("user") + "";
                String pass = option.get("password") + "";
                if (pass.length() == 32) {
                    creds.append(user);
                    creds.append(":::");
                    creds.append(pass);
                    creds.append(":::");
                } else {
                    creds.append(user);
                    creds.append(" ");
                    creds.append(pass);
                }
                creds.append("\n");
            }
            SafeDialogs.saveFile(null, "credentials.txt", f -> {
                CommonUtils.writeToFile(new File(f), CommonUtils.toBytes(creds.toString()));
                DialogUtils.showInfo("Exported Credentials");
            });
        }
    }

    public JComponent getContent() {
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.browser.notifyOnResult(this.dialog);
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton copy2 = new JButton("Copy");
        JButton del = new JButton("Remove");
        JButton export = new JButton("Export");
        JButton help = new JButton("Help");
        add.addActionListener(this);
        edit.addActionListener(this);
        copy2.addActionListener(this);
        del.addActionListener(this);
        export.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-credential-management"));
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(add, edit, copy2, export, del, help), "South");
        return this.dialog;
    }

}

