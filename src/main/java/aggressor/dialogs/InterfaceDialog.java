package aggressor.dialogs;

import aggressor.DataManager;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class InterfaceDialog implements DialogListener,
        Callback {
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected DataManager datal;
    protected Callback notifyme = null;
    private static int intno = 0;

    public void notify(Callback notifyme) {
        this.notifyme = notifyme;
    }

    public InterfaceDialog(TeamQueue conn, DataManager data) {
        this.conn = conn;
        this.datal = data;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String intf = DialogUtils.string(options, "INTERFACE");
        String hwaddr = DialogUtils.string(options, "HWADDRESS");
        String port = DialogUtils.string(options, "PORT");
        String channel = DialogUtils.string(options, "CHANNEL");
        this.conn.call("cloudstrike.start_tap", CommonUtils.args(intf, hwaddr, port, channel), this);
        if (this.notifyme != null) {
            this.notifyme.result("interface create", intf);
        }
    }

    @Override
    public void result(String call, Object result) {
        DialogUtils.showError(result + "");
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Setup Interface", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("INTERFACE", "phear" + intno);
        ++intno;
        controller.set("HWADDRESS", CommonUtils.randomMac());
        controller.set("PORT", CommonUtils.randomPort() + "");
        controller.set("CHANNEL", "UDP");
        controller.text("INTERFACE", "Interface:", 20);
        controller.text("HWADDRESS", "MAC Address:", 20);
        controller.text("PORT", "Local Port:", 20);
        controller.combobox("CHANNEL", "Channel:", new String[]{"HTTP", "ICMP", "TCP (Bind)", "TCP (Reverse)", "UDP"});
        JButton generate = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-covert-vpn");
        this.dialog.add(DialogUtils.description("Start a network interface and listener for CovertVPN. When a CovertVPN client is deployed, you will have a layer 2 tap into your target's network."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(generate, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

