
import aggressor.ui.UseSynthetica;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;

public class DialogTest {
    public static void main(String[] args) {
        new UseSynthetica().setup();
        JFrame dialog = DialogUtils.dialog("Hello World", 640, 480);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener((event, options) -> {
            System.err.println(options);
            System.exit(0);
        });
        controller.set("user", "msf");
        controller.set("pass", "test");
        controller.set("host", "127.0.0.1");
        controller.set("port", "55553");
        controller.text("user", "UserName:", 20);
        controller.text("pass", "Password:", 20);
        controller.text("host", "Host:", 20);
        controller.text("port", "Port:", 10);
        JButton ok = controller.action("OK");
        dialog.add(DialogUtils.description("This is the connect dialog"), "North");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok), "South");
        dialog.pack();
        dialog.setVisible(true);
    }

}

