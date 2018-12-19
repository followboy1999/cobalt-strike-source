package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class BypassUACDialog implements DialogListener {
    protected AggressorClient client;
    protected String[] bids;

    public BypassUACDialog(AggressorClient client, String[] bids) {
        this.client = client;
        this.bids = bids;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String listener = DialogUtils.string(options, "listener");
        TaskBeacon session = new TaskBeacon(this.client, this.bids);
        if (this.bids.length == 1) {
            DialogUtils.openOrActivate(this.client, this.bids[0]);
        }
        session.input("bypassuac " + listener);
        session.BypassUAC(listener);
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("Bypass UAC", 640, 480);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener(this);
        controller.listenerWithSMB("listener", "Listener:", this.client.getConnection(), this.client.getData());
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-bypassuac");
        dialog.add(DialogUtils.description("Execute a listener in a high-integrity context. This feature uses Cobalt Strike's Artifact Kit to generate an AV-safe DLL."), "North");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        dialog.pack();
        dialog.setVisible(true);
    }
}

