package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATextField;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class SecureShellPubKeyDialog implements DialogListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected ATextField user;
    protected ATextField pass;
    protected ATextField port;
    protected String[] targets;
    protected JCheckBox b;

    public SecureShellPubKeyDialog(AggressorClient client, String[] targets) {
        this.client = client;
        this.targets = targets;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String bid = DialogUtils.string(options, "bid");
        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{bid});
        String user = DialogUtils.string(options, "user");
        String key = DialogUtils.string(options, "key");
        String port = DialogUtils.string(options, "port");
        if ("".equals(user)) {
            DialogUtils.showError("You must specify a user");
            return;
        }
        if ("".equals(key)) {
            DialogUtils.showError("You must specify a key file");
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
        byte[] kdata = CommonUtils.readFile(key);
        DialogUtils.openOrActivate(this.client, bid);
        for (String tname : this.targets) {
            session.input("ssh-key " + tname + ":" + port + " " + user + " " + key);
            session.SecureShellPubKey(user, kdata, tname, CommonUtils.toNumber(port, 22));
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("SSH Login (Key)", 580, 350);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("port", "22");
        controller.text("user", "User:", 24);
        controller.file("key", "PEM File:");
        controller.text("port", "Port:", 10);
        controller.beacon("bid", "Session:", this.client);
        JButton ok = controller.action("Login");
        JButton help = controller.help("https://www.cobaltstrike.com/help-ssh-pubkey");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

