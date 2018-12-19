package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.AddressList;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import graph.Route;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PortScanLocalDialog extends AObject implements Callback, DialogListener {
    protected String bid;
    protected AggressorClient client;
    protected JFrame dialog = null;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"address", "netmask"};

    public PortScanLocalDialog(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
        this.model = DialogUtils.setupModel("address", this.cols, new LinkedList());
    }

    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String address = (String) this.model.getSelectedValueFromColumn(this.table, "address");
        String mask = (String) this.model.getSelectedValueFromColumn(this.table, "netmask");
        String discovery = DialogUtils.string(options, "discovery");
        String ports = DialogUtils.string(options, "ports");
        String sockets = DialogUtils.string(options, "sockets");
        String endaddr = AddressList.toIP(Route.ipToLong(address) + (Route.ipToLong("255.255.255.255") - Route.ipToLong(mask)));
        DialogUtils.openOrActivate(this.client, this.bid);
        TaskBeacon session = new TaskBeacon(this.client, new String[]{this.bid});
        session.input("portscan " + address + "-" + endaddr + " " + ports + " " + discovery + " " + sockets);
        session.PortScan(address + "-" + endaddr, ports, discovery, CommonUtils.toNumber(sockets, 1024));
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Scan", 480, 240);
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        JScrollPane scroll = new JScrollPane(this.table);
        scroll.setPreferredSize(new Dimension(scroll.getWidth(), 100));
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        Map defaults = new HashMap();
        defaults.put("ports", "1-1024,3389,5000-6000");
        defaults.put("discovery", "arp");
        defaults.put("sockets", "1024");
        controller.set(defaults);
        controller.text("ports", "Ports:");
        controller.text("sockets", "Max Sockets:");
        controller.combobox("discovery", "Discovery:", CommonUtils.toArray("arp, icmp, none"));
        JButton chooser = controller.action("Scan");
        JButton help = controller.help("https://www.cobaltstrike.com/help-portscan");
        this.dialog.add(scroll, "Center");
        this.dialog.add(DialogUtils.stackTwo(controller.layout(), DialogUtils.center(chooser, help)), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.client.getConnection().call("beacons.task_ipconfig", CommonUtils.args(this.bid), this);
    }

    public void result(String key, Object o) {
        LinkedList results = CommonUtils.parseTabData(o + "", CommonUtils.toArray("address, netmask"));

        for (Object result : results) {
            Map temp = (Map) result;
            String address = (String) temp.get("address");
            String mask = (String) temp.get("netmask");
            String startl = AddressList.toIP(Route.ipToLong(address) & Route.ipToLong(mask));
            temp.put("address", startl);
        }

        DialogUtils.setTable(this.table, this.model, results);
    }
}
