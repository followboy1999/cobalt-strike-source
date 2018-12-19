package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Credentials;
import beacon.TaskBeacon;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATextField;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class SpawnAsDialog implements DialogListener,
        ListSelectionListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected ATextField user;
    protected ATextField pass;
    protected ATextField domain;
    protected Credentials browser;
    protected String bid;

    public SpawnAsDialog(AggressorClient client, String bid) {
        this.client = client;
        this.browser = new Credentials(client);
        this.browser.setColumns("user, password, realm, note");
        this.browser.noHashes();
        this.bid = bid;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String listener = DialogUtils.string(options, "listener");
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        session.input("spawnas " + this.domain.getText() + "\\" + this.user.getText() + " " + this.pass.getText());
        session.SpawnAs(this.domain.getText(), this.user.getText(), this.pass.getText(), listener);
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

    public void show() {
        this.dialog = DialogUtils.dialog("Spawn As", 580, 400);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        JComponent credbrowser = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) controller.text("user", "User:", 36).get(1);
        this.pass = (ATextField) controller.text("pass", "Password:", 36).get(1);
        this.domain = (ATextField) controller.text("domain", "Domain:", 36).get(1);
        controller.listenerWithSMB("listener", "Listener:", this.client.getConnection(), this.client.getData());
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-spawnas");
        this.dialog.add(credbrowser, "Center");
        this.dialog.add(DialogUtils.stackTwo(controller.layout(), DialogUtils.center(ok, help)), "South");
        this.dialog.setVisible(true);
    }
}

