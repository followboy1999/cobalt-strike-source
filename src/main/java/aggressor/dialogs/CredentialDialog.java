package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class CredentialDialog implements DialogListener {
    protected JFrame dialog = null;
    protected Map options;
    protected String title = "Edit Credential";
    protected AggressorClient client;

    public CredentialDialog(AggressorClient client) {
        this(client, new HashMap());
        this.title = "New Credential";
        this.options.put("source", "manual");
    }

    public CredentialDialog(AggressorClient client, Map options) {
        this.client = client;
        this.options = new HashMap(options);
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String newc;
        String oldc = CommonUtils.CredKey(this.options);
        if (!oldc.equals(newc = CommonUtils.CredKey(options))) {
            this.client.getConnection().call("credentials.remove", CommonUtils.args(oldc));
        }
        this.client.getConnection().call("credentials.add", CommonUtils.args(newc, options));
        this.client.getConnection().call("credentials.push");
        this.options = this.title.equals("Edit Credential") ? new HashMap(options) : new HashMap();
    }

    public void show() {
        this.dialog = DialogUtils.dialog(this.title, 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set(this.options);
        controller.text("user", "User:", 20);
        controller.text("password", "Password:", 20);
        controller.text("realm", "Realm:", 20);
        controller.text("note", "Note:", 20);
        controller.combobox("source", "Source:", CommonUtils.toArray("hashdump, manual, mimikatz"));
        JButton ok = controller.action("Save");
        this.dialog.add(DialogUtils.description("Edit credential store."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

