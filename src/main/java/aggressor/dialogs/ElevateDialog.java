package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ElevateDialog implements DialogListener {
    protected AggressorClient client;
    protected String[] bids;

    public ElevateDialog(AggressorClient client, String[] bids) {
        this.client = client;
        this.bids = bids;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String exploit = DialogUtils.string(options, "exploit");
        String listener = DialogUtils.string(options, "listener");
        TaskBeacon session = new TaskBeacon(this.client, this.bids);
        if (this.bids.length == 1) {
            DialogUtils.openOrActivate(this.client, this.bids[0]);
        }
        session.input("elevate " + exploit + " " + listener);
        session.Elevate(exploit, listener);
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("Elevate", 640, 480);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener(this);
        controller.listenerWithSMB("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.exploits("exploit", "Exploit:", this.client);
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-elevate");
        dialog.add(DialogUtils.description("Attempt to execute a listener in an elevated context."), "North");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        dialog.pack();
        dialog.setVisible(true);
    }
}

