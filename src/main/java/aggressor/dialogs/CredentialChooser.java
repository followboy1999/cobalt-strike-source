package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Credentials;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import filter.DataFilter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class CredentialChooser implements DialogListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected Credentials browser;
    protected SafeDialogCallback callback;

    public CredentialChooser(AggressorClient client, SafeDialogCallback callback) {
        this.client = client;
        this.callback = callback;
        this.browser = new Credentials(client);
        this.browser.setColumns("user, password, realm, note");
    }

    public DataFilter getFilter() {
        return this.browser.getFilter();
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String user = (String) this.browser.getSelectedValueFromColumn("user");
        String realm = (String) this.browser.getSelectedValueFromColumn("realm");
        String pass = (String) this.browser.getSelectedValueFromColumn("password");
        if (realm == null || realm.length() == 0) {
            this.callback.dialogResult(user + " " + pass);
        } else {
            this.callback.dialogResult(realm + "\\" + user + " " + pass);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a Credential", 580, 200);
        this.dialog.addWindowListener(this.browser.onclose());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        JComponent credbrowser = this.browser.getContent();
        JButton choose = controller.action("Choose");
        this.dialog.add(credbrowser, "Center");
        this.dialog.add(DialogUtils.center(choose), "South");
        this.dialog.setVisible(true);
    }
}

