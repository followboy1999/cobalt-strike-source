package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Credentials;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATextField;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class SecureShellDialog implements DialogListener,
        ListSelectionListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected ATextField user;
    protected ATextField pass;
    protected ATextField port;
    protected Credentials browser;
    protected String[] targets;
    protected JCheckBox b;

    public SecureShellDialog(AggressorClient client, String[] targets) {
        this.client = client;
        this.browser = new Credentials(client);
        this.targets = targets;
        this.browser.setColumns("user, password, realm, note");
        this.browser.noHashes();
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String bid = DialogUtils.string(options, "bid");
        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{bid});
        String user = DialogUtils.string(options, "user");
        String pass = DialogUtils.string(options, "pass");
        String port = DialogUtils.string(options, "port");
        if ("".equals(user)) {
            DialogUtils.showError("You must specify a user");
            return;
        }
        if ("".equals(pass)) {
            DialogUtils.showError("You must specify a password");
            return;
        }
        if ("".equals(port)) {
            DialogUtils.showError("You must specify a port");
            return;
        }
        if (entry == null) {
            DialogUtils.showError("You must select a Beacon session!");
            return;
        }
        DialogUtils.openOrActivate(this.client, bid);
        for (String tname : this.targets) {
            session.input("ssh " + tname + ":" + port + " " + user + " " + pass);
            session.SecureShell(user, pass, tname, CommonUtils.toNumber(port, 22));
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        this.user.setText((String) this.browser.getSelectedValueFromColumn("user"));
        this.pass.setText((String) this.browser.getSelectedValueFromColumn("password"));
    }

    public void show() {
        this.dialog = DialogUtils.dialog("SSH Login", 580, 350);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("port", "22");
        JComponent credbrowser = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) controller.text("user", "User:", 36).get(1);
        this.pass = (ATextField) controller.text("pass", "Password:", 36).get(1);
        controller.text("port", "Port:", 10);
        controller.beacon("bid", "Session:", this.client);
        JButton ok = controller.action("Login");
        JButton help = controller.help("https://www.cobaltstrike.com/help-ssh");
        this.dialog.add(credbrowser, "Center");
        this.dialog.add(DialogUtils.stack(controller.layout(), DialogUtils.center(ok, help)), "South");
        this.dialog.setVisible(true);
    }
}

