package aggressor.dialogs;

import aggressor.AggressorClient;
import common.AddressList;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class TargetDialog implements DialogListener {
    protected JFrame dialog = null;
    protected AggressorClient client;

    public TargetDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> _options) {
        HashMap<String, Object> options = new HashMap<>(_options);
        String targets = DialogUtils.string(_options, "address");
        String os = DialogUtils.string(options, "os");
        switch (os) {
            case "Windows 2000":
                options.put("os", "Windows");
                options.put("version", "5.0");
                break;
            case "Windows XP":
                options.put("os", "Windows");
                options.put("version", "5.1");
                break;
            case "Windows 7":
                options.put("os", "Windows");
                options.put("version", "6.0");
                break;
            case "Windows 8.1":
                options.put("os", "Windows");
                options.put("version", "6.2");
                break;
            case "Windows 10":
                options.put("os", "Windows");
                options.put("version", "10.0");
                break;
        }
        for (Object o : new AddressList(targets).toList()) {
            HashMap<String, Object> next = new HashMap<>(options);
            next.put("address", o);
            this.client.getConnection().call("targets.add", CommonUtils.args(CommonUtils.TargetKey(next), next));
        }
        this.client.getConnection().call("targets.push");
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Add Target", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set(CommonUtils.toMap("os", "Windows 7"));
        controller.text("address", "Address:", 20);
        controller.text("name", "Name:", 20);
        controller.combobox("os", "os:", CommonUtils.toArray("Android, Apple iOS, Cisco IOS, Firewall, FreeBSD, Linux, MacOS X, NetBSD, OpenBSD, Printer, Unknown, VMware, Windows 2000, Windows XP, Windows 7, Windows 8.1, Windows 10"));
        controller.text("note", "Note:", 20);
        JButton ok = controller.action("Save");
        this.dialog.add(DialogUtils.description("Add a new target."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

