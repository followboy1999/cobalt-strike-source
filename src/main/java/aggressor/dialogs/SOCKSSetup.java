package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class SOCKSSetup
        extends AObject implements DialogListener {
    protected String bid;
    protected AggressorClient client;
    protected JFrame dialog = null;

    public SOCKSSetup(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        int port = DialogUtils.number(options, "ProxyPort");
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        session.input("socks " + port);
        session.SocksStart(port);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Start SOCKS", 240, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("ProxyPort", CommonUtils.randomPort() + "");
        controller.text("ProxyPort", "Proxy Server Port:", 8);
        JButton chooser = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-socks-proxy-pivoting");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(chooser, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

