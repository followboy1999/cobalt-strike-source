package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class PortScanDialog
        extends AObject implements DialogListener {
    protected String[] targets;
    protected AggressorClient client;

    public PortScanDialog(AggressorClient client, String[] targets) {
        this.client = client;
        this.targets = targets;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String ports = DialogUtils.string(options, "ports");
        String bid = DialogUtils.string(options, "bid");
        String tgz = CommonUtils.join(this.targets, ",");
        String maxsocks = DialogUtils.string(options, "sockets");
        if ("".equals(bid)) {
            DialogUtils.showError("You must select a Beacon session to scan through.");
        } else {
            TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{bid});
            DialogUtils.openOrActivate(this.client, bid);
            session.input("portscan " + tgz + " " + ports + " none " + maxsocks);
            session.PortScan(tgz, ports, "none", CommonUtils.toNumber(maxsocks, 1024));
        }
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("Scan", 480, 240);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener(this);
        HashMap<String, String> defz = new HashMap<>();
        defz.put("ports", "1-1024,3389,5900-6000");
        defz.put("sockets", "1024");
        controller.set(defz);
        controller.text("ports", "Ports", 25);
        controller.text("sockets", "Max Sockets", 25);
        controller.beacon("bid", "Session", this.client);
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-portscan");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        dialog.pack();
        dialog.setVisible(true);
    }
}

