package aggressor.dialogs;

import aggressor.AggressorClient;
import common.ArtifactUtils;
import common.CommonUtils;
import common.PowerShellUtils;
import common.ResourceUtils;
import dialog.*;
import encoders.Transforms;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PayloadGeneratorDialog implements DialogListener,
        SafeDialogCallback {
    protected JFrame dialog = null;
    protected byte[] stager = null;
    protected AggressorClient client;
    protected Map options = null;

    public PayloadGeneratorDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        this.stager = DialogUtils.getStager(options);
        if (this.stager.length == 0) {
            return;
        }
        Map extension = DialogUtils.toMap("ASPX: aspx, C: c, C#: cs, HTML Application: hta, Java: java, Perl: pl, PowerShell: ps1, PowerShell Command: txt, Python: py, Raw: bin, Ruby: rb, COM Scriptlet: sct, Veil: txt, VBA: vba");
        String type = DialogUtils.string(options, "format");
        String ext = "payload." + extension.get(type);
        SafeDialogs.saveFile(null, ext, this);
    }

    @Override
    public void dialogResult(String outfile) {
        String type = DialogUtils.string(this.options, "format");
        boolean x64 = DialogUtils.bool(this.options, "x64");
        String listener = DialogUtils.string(this.options, "listener");
        if (type.equals("C")) {
            this.stager = Transforms.toC(this.stager);
        } else if (type.equals("C#")) {
            this.stager = Transforms.toCSharp(this.stager);
        } else if (type.equals("Java")) {
            this.stager = Transforms.toJava(this.stager);
        } else if (type.equals("Perl")) {
            this.stager = Transforms.toPerl(this.stager);
        } else if (type.equals("PowerShell") && x64) {
            this.stager = new ResourceUtils(this.client).buildPowerShell(this.stager, true);
        } else if (type.equals("PowerShell") && !x64) {
            this.stager = new ResourceUtils(this.client).buildPowerShell(this.stager);
        } else if (type.equals("PowerShell Command") && x64) {
            this.stager = new PowerShellUtils(this.client).buildPowerShellCommand(this.stager, true);
        } else if (type.equals("PowerShell Command") && !x64) {
            this.stager = new PowerShellUtils(this.client).buildPowerShellCommand(this.stager, false);
        } else if (type.equals("Python")) {
            this.stager = Transforms.toPython(this.stager);
        } else if (!type.equals("Raw")) {
            switch (type) {
                case "Ruby":
                    this.stager = Transforms.toPython(this.stager);
                    break;
                case "COM Scriptlet":
                    if (x64) {
                        DialogUtils.showError(type + " is not compatible with x64 stagers");
                        return;
                    }
                    this.stager = new ArtifactUtils(this.client).buildSCT(this.stager);
                    break;
                case "Veil":
                    this.stager = Transforms.toVeil(this.stager);
                    break;
                case "VBA":
                    this.stager = CommonUtils.toBytes("myArray = " + Transforms.toVBA(this.stager));
                    break;
            }
        }
        CommonUtils.writeToFile(new File(outfile), this.stager);
        DialogUtils.showInfo("Saved " + type + " to\n" + outfile);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Payload Generator", 640, 480);
        String[] options = CommonUtils.toArray("C, C#, COM Scriptlet, Java, Perl, PowerShell, PowerShell Command, Python, Raw, Ruby, Veil, VBA");
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("format", "raw");
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.combobox("format", "Output:", options);
        controller.checkbox_add("x64", "x64:", "Use x64 payload");
        JButton ok = controller.action("Generate");
        JButton help = controller.help("https://www.cobaltstrike.com/help-payload-generator");
        this.dialog.add(DialogUtils.description("This dialog generates a payload to stage a Cobalt Strike listener. Several output options are available."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

