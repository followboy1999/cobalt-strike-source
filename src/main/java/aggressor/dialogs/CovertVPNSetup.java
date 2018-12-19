package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;

public class CovertVPNSetup
        extends AObject implements Callback,
        DialogListener {
    protected String bid;
    protected AggressorClient client;
    protected JFrame dialog = null;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"IPv4 Address", "IPv4 Netmask", "Hardware MAC"};

    public CovertVPNSetup(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
        this.model = DialogUtils.setupModel("IPv4 Address", this.cols, new LinkedList());
    }

    public void refresh() {
        this.client.getConnection().call("beacons.task_ipconfig", CommonUtils.args(this.bid), this);
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String intf = DialogUtils.string(options, "VPNInterface");
        String ip = this.model.getSelectedValueFromColumn(this.table, "IPv4 Address") + "";
        String mac = this.model.getSelectedValueFromColumn(this.table, "Hardware MAC") + "";
        if (!DialogUtils.bool(options, "CloneMAC")) {
            mac = null;
        }
        if (options.get("VPNInterface") == null) {
            DialogUtils.showError("Please select or add a VPN interface");
        } else if (this.model.getSelectedValueFromColumn(this.table, "IPv4 Address") == null) {
            DialogUtils.showError("Please select a network interface");
        } else {
            if (!DialogUtils.isShift(event)) {
                this.dialog.setVisible(false);
            }
            DialogUtils.openOrActivate(this.client, this.bid);
            TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
            session.input("covertvpn " + intf + " " + ip);
            session.CovertVPN(this.bid, intf, ip, mac);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Deploy VPN Client", 480, 240);
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        JScrollPane scroll = new JScrollPane(this.table);
        scroll.setPreferredSize(new Dimension(scroll.getWidth(), 100));
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("CloneMAC", "true");
        controller.interfaces("VPNInterface", "Local Interface: ", this.client.getConnection(), this.client.getData());
        JComponent a = controller.row();
        JCheckBox b = controller.checkbox("CloneMAC", "Clone host MAC address");
        JButton chooser = controller.action_noclose("Deploy");
        JButton help = controller.help("https://www.cobaltstrike.com/help-covert-vpn");
        JPanel middle = new JPanel();
        middle.setLayout(new BorderLayout());
        middle.add(a, "North");
        middle.add(b, "Center");
        middle.add(DialogUtils.center(chooser, help), "South");
        this.dialog.add(scroll, "Center");
        this.dialog.add(middle, "South");
        this.refresh();
        this.dialog.setVisible(true);
    }

    @Override
    public void result(String key, Object o) {
        LinkedList results = CommonUtils.parseTabData(o + "", CommonUtils.toArray("IPv4 Address, IPv4 Netmask, MTU, Hardware MAC"));
        DialogUtils.setTable(this.table, this.model, results);
    }
}

