package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.MultiFrame;
import aggressor.Prefs;
import aggressor.windows.PhishLog;
import common.CommonUtils;
import common.TeamQueue;
import common.UploadFile;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class SpearPhishDialog implements DialogListener,
        UploadFile.UploadNotify {
    protected MultiFrame window;
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected DataManager datal;
    protected AggressorClient client;
    protected Map options = null;
    protected LinkedList contacts;
    protected String attachment;
    protected String bounce;
    protected String mailserver;
    protected String template;
    protected String server;

    public SpearPhishDialog(AggressorClient client, MultiFrame window, TeamQueue conn, DataManager data) {
        this.window = window;
        this.conn = conn;
        this.datal = data;
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        if ("Preview".equals(event.getActionCommand())) {
            this.preview(options);
        } else {
            this.send(event, options);
        }
        this.save(options);
    }

    public void preview(Map options) {
        LinkedList contacts = (LinkedList) options.get("targets");
        String template = DialogUtils.string(options, "template");
        if (template == null || !new File(template).exists()) {
            DialogUtils.showError("I need a template to show you a preview!");
        } else if (contacts == null || contacts.size() == 0) {
            DialogUtils.showError("I need a target to show you a preview!");
        } else {
            new MailPreview(options).show();
        }
    }

    public boolean checkContactsReverse(LinkedList contacts) {
        Iterator i = contacts.iterator();
        int x = 1;
        while (i.hasNext()) {
            Map entry = (Map) i.next();
            String to = DialogUtils.string(entry, "To");
            String name = DialogUtils.string(entry, "To_Name");
            if (name.length() > 0 && name.indexOf(64) > 0 && to.indexOf(64) < 0) {
                DialogUtils.showError("Your target file is in the wrong format.\nPlease check that the format is:\n\nuser@target<TAB>User's Name\n\nLook at entry " + x + ":\n" + to + "<TAB>" + name);
                return true;
            }
            ++x;
        }
        return false;
    }

    public void send(ActionEvent event, Map options) {
        this.options = options;
        this.contacts = (LinkedList) options.get("targets");
        this.template = DialogUtils.string(options, "template");
        this.bounce = DialogUtils.string(options, "bounce");
        this.attachment = DialogUtils.string(options, "attachment");
        this.server = DialogUtils.string(options, "server");
        if (this.contacts == null || this.contacts.size() == 0) {
            DialogUtils.showError("Please import a target file");
            return;
        }
        if (this.checkContactsReverse(this.contacts)) {
            return;
        }
        if ("".equals(this.template)) {
            DialogUtils.showError("Please choose a template message");
            return;
        }
        if ("".equals(this.bounce)) {
            DialogUtils.showError("Please provide a bounce address");
            return;
        }
        if (!new File(this.template).exists()) {
            DialogUtils.showError("The template does not exist");
            return;
        }
        if (!"".equals(this.attachment) && !new File(this.attachment).exists()) {
            DialogUtils.showError("Hey, the attachment doesn't exist");
            return;
        }
        if ("".equals(this.server)) {
            DialogUtils.showError("I need a server to send phishes through.");
            return;
        }
        if (this.server.startsWith("http://")) {
            DialogUtils.showError("Common mistake! The mail server is a host:port, not a URL");
            return;
        }
        if (!DialogUtils.isShift(event)) {
            this.dialog.setVisible(false);
        }
        if (!"".equals(this.attachment)) {
            new UploadFile(this.conn, new File(this.attachment), this).start();
        } else {
            this.send_phish();
        }
    }

    @Override
    public void complete(String name) {
        this.options.put("attachmentr", name);
        this.send_phish();
    }

    @Override
    public void cancel() {
        this.dialog.setVisible(true);
    }

    public void send_phish() {
        String sid = CommonUtils.ID();
        PhishLog log = new PhishLog(sid, this.datal, this.client.getScriptEngine(), this.conn);
        this.client.getTabManager().addTab("send email", log.getConsole(), log.cleanup(), "Transcript of phishing activity");
        String template_data = CommonUtils.bString(CommonUtils.readFile(this.template));
        this.conn.call("cloudstrike.go_phish", CommonUtils.args(sid, template_data, new HashMap(this.options)));
    }

    public void save(Map options) {
        Prefs.getPreferences().set("cloudstrike.send_email_bounce.string", (String) options.get("bounce"));
        Prefs.getPreferences().set("cloudstrike.send_email_server.string", (String) options.get("server"));
        Prefs.getPreferences().set("cloudstrike.send_email_target.file", (String) options.get("_targets"));
        Prefs.getPreferences().save();
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Spear Phish", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("bounce", Prefs.getPreferences().getString("cloudstrike.send_email_bounce.string", ""));
        controller.set("server", Prefs.getPreferences().getString("cloudstrike.send_email_server.string", ""));
        controller.set("_targets", Prefs.getPreferences().getString("cloudstrike.send_email_target.file", ""));
        GenericTableModel model = DialogUtils.setupModel("To", CommonUtils.toArray("To, To_Name"), new LinkedList());
        ATable table = DialogUtils.setupTable(model, CommonUtils.toArray("To, To_Name"), false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(scroll.getWidth(), 150));
        controller.file_import("targets", "Targets:", table, model);
        controller.file("template", "Template:");
        controller.file("attachment", "Attachment:");
        controller.site("url", "Embed URL:", this.conn, this.datal);
        controller.mailserver("server", "Mail Server:");
        controller.text("bounce", "Bounce To:", 30);
        JButton preview = controller.action_noclose("Preview");
        JButton send = controller.action_noclose("Send");
        JButton help = controller.help("https://www.cobaltstrike.com/help-spear-phish");
        this.dialog.add(scroll);
        this.dialog.add(DialogUtils.stackTwo(controller.layout(), DialogUtils.center(preview, send, help)), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

