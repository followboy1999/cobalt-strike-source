package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class ListenerDialog implements DialogListener, Callback, SafeDialogCallback {
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected Map<String, Object> options;
    protected String title = "Edit Listener";
    protected Callback notifyme = null;
    protected DataManager datal;

    public void notify(Callback callback) {
        this.notifyme = callback;
    }

    public ListenerDialog(TeamQueue teamQueue, DataManager dataManager) {
        this(teamQueue, dataManager, new HashMap<>());
        this.title = "New Listener";
        this.options.put("host", DataUtils.getLocalIP(dataManager));
        this.options.put("beacons", DataUtils.getLocalIP(dataManager));
        this.options.put("payload", "windows/beacon_http/reverse_http");
    }

    public ListenerDialog(TeamQueue teamQueue, DataManager dataManager, HashMap<String, Object> map2) {
        this.conn = teamQueue;
        this.options = map2;
        this.datal = dataManager;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        String name = (String) options.get("name");
        String host = (String) options.get("host");
        String payload = (String) options.get("payload");
        String string4 = (String) options.get("port");
        int n;
        if (!options.containsKey("beacons")) {
            options.put("beacons", "");
        }
        try {
            n = Integer.parseInt(string4);
        } catch (Exception exception) {
            n = -1;
        }
        if (name == null || "".equals(name)) {
            DialogUtils.showError("Heh?!? Your listener needs a name");
        } else if (payload == null || "".equals(payload)) {
            DialogUtils.showError("Dude, you need to select a payload");
        } else if (host == null || "".equals(host)) {
            DialogUtils.showError("A host is required for a listener");
        } else if (host.contains(",") || host.contains(" ")) {
            DialogUtils.showError("Please specify one host in the host field");
        } else if (string4 == null) {
            DialogUtils.showError("A port is required for a listener");
        } else if (n < 0 || n > 65535) {
            DialogUtils.showError("Port " + n + " is out of range.");
        } else if (payload.equals("windows/beacon_dns/reverse_dns_txt") && n == 53) {
            DialogUtils.showError("The host/port of this listener sets up an HTTP handler\nfor HTTP requests [when in mode http]. Please use a\nport other than 53.");
        } else if (payload.equals("windows/foreign/reverse_dns_txt") && n != 53) {
            DialogUtils.showError("The DNS foreign listener should always point to port 53.");
        } else if (payload.equals("windows/foreign/reverse_dns_txt") && CommonUtils.isIP(host)) {
            DialogUtils.showError("This foreign lister stages a payload over DNS. The\nconfigured host must be a fully qualified domain that\nyour remote Cobalt Strike instance is authoritative for.");
        } else if (payload.equals("windows/beacon_smb/bind_pipe") && n == 445) {
            DialogUtils.showError("This listener uses Port 445 to deliver an SMB Beacon\nover a named pipe to a remote host. If the local system\nis the target, this payload will use a bind_tcp stager.\nPlease don't specify port 445 as that won't work :)");
        } else {
            if (!DialogUtils.isShift(event)) {
                this.dialog.setVisible(false);
            }
            switch (payload) {
                case "windows/beacon_dns/reverse_http":
                    SafeDialogs.ask("This beacon uses DNS to check for taskings. Please provide the\ndomains to use for beaconing. The NS record for these domains\nmust point to your Cobalt Strike system. Separate multiple\ndomains with a comma", options.get("beacons") + "", this);
                    break;
                case "windows/beacon_dns/reverse_dns_txt":
                    SafeDialogs.ask("This beacon uses DNS to check for taskings. Please provide the\ndomains to use for beaconing. The NS record for these domains\nmust point to your Cobalt Strike system. Separate multiple\ndomains with a comma", options.get("beacons") + "", this);
                    break;
                case "windows/beacon_http/reverse_http":
                case "windows/beacon_https/reverse_https":
                    SafeDialogs.ask("This beacon uses HTTP to check for taskings. Please provide the\ndomains to use for beaconing. The A record for these domains\nmust point to your Cobalt Strike system. An IP address is OK.\nSeparate each host or domain with a comma.", options.get("beacons") + "", this);
                    break;
                default:
                    this.dialogResult("");
            }
        }
    }

    @Override
    public void dialogResult(String string) {
        Object string2 = this.options.get("name");
        if (string == null) {
            return;
        }
        if (string.length() > 255) {
            DialogUtils.showError("Make domain list less than 255 characters");
            return;
        }
        this.options.put("beacons", string);
        this.conn.call("listeners.stop", CommonUtils.args(string2), this);
        this.conn.call("listeners.create", CommonUtils.args(string2, this.options), this);
        if (this.notifyme != null) {
            this.notifyme.result("listener create", string2);
        }
    }

    @Override
    public void result(String string, Object object) {
        String string2 = object + "";
        if (string2.equals("success")) {
            DialogUtils.showInfo("Started Listener");
        } else {
            DialogUtils.showError("Could not start listener: \n" + string2);
        }
    }

    public void show() {
        String[] arrobject = CommonUtils.toArray("windows/beacon_dns/reverse_dns_txt, windows/beacon_dns/reverse_http, windows/beacon_http/reverse_http, windows/beacon_https/reverse_https, windows/beacon_smb/bind_pipe, windows/foreign/reverse_http, windows/foreign/reverse_https, windows/foreign/reverse_tcp");
        this.dialog = DialogUtils.dialog(this.title, 640, 480);
        DialogManager dialogManager = new DialogManager(this.dialog);
        dialogManager.addDialogListener(this);
        dialogManager.set(this.options);
        if (this.title.equals("Edit Listener")) {
            dialogManager.text_disabled("name", "Name:");
        } else {
            dialogManager.text("name", "Name:", 20);
        }
        dialogManager.combobox("payload", "Payload:", arrobject);
        dialogManager.text("host", "Host:", 20);
        dialogManager.text("port", "Port:", 10);
        JButton jButton = dialogManager.action_noclose("Save");
        this.dialog.add(DialogUtils.description("Create a listener."), "North");
        this.dialog.add(dialogManager.layout(), "Center");
        this.dialog.add(DialogUtils.center(jButton), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

