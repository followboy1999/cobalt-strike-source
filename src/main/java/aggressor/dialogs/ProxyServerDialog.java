package aggressor.dialogs;

import common.AObject;
import common.CommonUtils;
import common.ProxyServer;
import dialog.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class ProxyServerDialog
        extends AObject implements DialogListener {
    protected JFrame dialog = null;
    protected SafeDialogCallback callback;
    protected String oldv;

    public ProxyServerDialog(String oldvalue, SafeDialogCallback callback) {
        this.callback = callback;
        this.oldv = oldvalue;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        if ("Reset".equals(event.getActionCommand())) {
            this.callback.dialogResult("");
        } else {
            ProxyServer server = ProxyServer.resolve(options);
            this.callback.dialogResult(server.toString());
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("(Manual) Proxy Settings", 320, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        LightSwitch dimmer = new LightSwitch();
        controller.set(ProxyServer.parse(this.oldv).toMap());
        controller.combobox("ptype", "Proxy Type: ", CommonUtils.toArray("http, socks"));
        controller.text("phost", "Proxy Host: ", 20);
        controller.text("pport", "Proxy Port: ", 20);
        controller.text("puser", "Username: ", 20);
        controller.text("ppass", "Password: ", 20);
        dimmer.add(controller.getRows());
        dimmer.set(controller.checkbox_add("pdirect", "", "Ignore proxy settings; use direct connection"), true);
        JButton set = controller.action("Set");
        JButton defaults = controller.action("Reset");
        JButton help = controller.help("https://www.cobaltstrike.com/help-http-beacon#proxy");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(set, defaults, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}

