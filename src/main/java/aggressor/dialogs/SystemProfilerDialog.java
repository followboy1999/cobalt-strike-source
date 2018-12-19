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

public class SystemProfilerDialog implements DialogListener,
        Callback {
    protected MultiFrame window;
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected DataManager datal;
    protected String port;
    protected String uri;
    protected String java;
    protected String redir;
    protected String host;
    protected String proto;
    protected boolean ssl;

    public SystemProfilerDialog(MultiFrame window, TeamQueue conn, DataManager data) {
        this.window = window;
        this.conn = conn;
        this.datal = data;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.java = DialogUtils.string(options, "java");
        this.redir = DialogUtils.string(options, "redirect");
        this.uri = DialogUtils.string(options, "uri");
        this.port = DialogUtils.string(options, "port");
        this.host = DialogUtils.string(options, "host");
        this.ssl = DialogUtils.bool(options, "ssl");
        String string = this.proto = this.ssl ? "https://" : "http://";
        if (!"".equals(this.redir)) {
            this.conn.call("cloudstrike.start_profiler", CommonUtils.args(this.host, Integer.parseInt(this.port), this.ssl, this.uri, this.redir, this.java, "System Profiler. Redirects to " + this.redir), this);
        } else {
            this.conn.call("cloudstrike.start_profiler", CommonUtils.args(this.host, Integer.parseInt(this.port), this.ssl, this.uri, null, this.java, "System Profiler"), this);
        }
    }

    @Override
    public void result(String method, Object data) {
        String status = data + "";
        if ("success".equals(status)) {
            DialogUtils.startedWebService("system profiler", this.proto + this.host + ":" + this.port + this.uri);
        } else {
            DialogUtils.showError("Unable to start profiler:\n" + status);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("System Profiler", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("uri", "/");
        controller.set("port", "80");
        controller.set("java", "true");
        controller.set("host", DataUtils.getLocalIP(this.datal));
        controller.text("uri", "Local URI:", 20);
        controller.text("host", "Local Host:", 20);
        controller.text("port", "Local Port:", 20);
        controller.text("redirect", "Redirect URL:", 20);
        controller.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.datal));
        controller.checkbox_add("java", "", "Use Java Applet to get information");
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-system-profiler");
        this.dialog.add(DialogUtils.description("The system profiler is a client-side reconaissance tool. It finds common applications (with version numbers) used by the user."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

