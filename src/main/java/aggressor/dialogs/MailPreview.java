package aggressor.dialogs;

import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import console.Display;
import dialog.DialogUtils;
import mail.Eater;
import phish.PhishingUtils;
import ui.ATextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public class MailPreview
        extends AObject implements ActionListener {
    protected JFrame dialog = null;
    protected String templatef;
    protected String attachf;
    protected LinkedList contacts;
    protected String urlv;
    protected String cRaw = null;
    protected String cHtml = null;
    protected String cText = null;

    public boolean processOptions() {
        try {
            this._processOptions();
            return true;
        } catch (Exception ex) {
            DialogUtils.showError("Trouble processing " + this.templatef + ":\n" + ex.getMessage());
            MudgeSanity.logException("process phishing preview", ex, false);
            return false;
        }
    }

    public void _processOptions() throws IOException {
        Eater template = new Eater(this.templatef);
        if (!"".equals(this.attachf) && this.attachf.length() > 0 && new File(this.attachf).exists()) {
            template.attachFile(this.attachf);
        }
        Map target = (Map) CommonUtils.pick(this.contacts);
        String to = (String) target.get("To");
        String tname = target.get("To_Name") + "";
        byte[] message = template.getMessage(null, tname.length() > 0 ? tname + " <" + to + ">" : to);
        String messagez = PhishingUtils.updateMessage(CommonUtils.bString(message), target, this.urlv, "1234567890ab");
        Eater parser = new Eater(new ByteArrayInputStream(CommonUtils.toBytes(messagez)));
        this.cHtml = parser.getMessageEntity("text/html");
        this.cText = parser.getMessageEntity("text/plain");
        this.cRaw = messagez;
        template.done();
        parser.done();
    }

    public MailPreview(Map options) {
        this.templatef = DialogUtils.string(options, "template");
        this.attachf = DialogUtils.string(options, "attachment");
        this.contacts = (LinkedList) options.get("targets");
        this.urlv = DialogUtils.string(options, "url");
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        this.dialog.setVisible(false);
        this.dialog.dispose();
    }

    public JComponent buildRaw() {
        Display text = new Display(new Properties());
        text.setFont(Font.decode("Monospaced BOLD 14"));
        text.setForeground(Color.decode("#ffffff"));
        text.setBackground(Color.decode("#000000"));
        text.setTextDirect(this.cRaw);
        return text;
    }

    public byte[] buildHTMLScreenshot() {
        JEditorPane text = new JEditorPane();
        text.setContentType("text/html");
        DialogUtils.workAroundEditorBug(text);
        text.setEditable(false);
        text.setOpaque(true);
        text.setCaretPosition(0);
        text.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        text.setText(this.cHtml);
        text.setSize(new Dimension(640, 480));
        return DialogUtils.screenshot(text);
    }

    public JComponent buildHTML() {
        final ATextField block = new ATextField();
        final JEditorPane text = new JEditorPane();
        text.setContentType("text/html");
        DialogUtils.workAroundEditorBug(text);
        text.setEditable(false);
        text.setOpaque(true);
        text.setCaretPosition(0);
        text.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        text.addHyperlinkListener(ev -> {
            String type = ev.getEventType() + "";
            switch (type) {
                case "ENTERED":
                    block.setText(ev.getURL() + "");
                    block.setCaretPosition(0);
                    break;
                case "EXITED":
                    block.setText("");
                    break;
                case "ACTIVATED":
                    DialogUtils.showInput(MailPreview.this.dialog, "You clicked", ev.getURL() + "");
                    break;
            }
        });
        new Thread(() -> text.setText(MailPreview.this.cHtml), "buildHTML").start();
        JPanel tab = new JPanel();
        tab.setLayout(new BorderLayout());
        tab.add(new JScrollPane(text), "Center");
        tab.add(block, "South");
        return tab;
    }

    public JComponent buildText() {
        JEditorPane text = new JEditorPane();
        text.setContentType("text/plain");
        text.setText(this.cText);
        text.setEditable(false);
        text.setOpaque(true);
        text.setCaretPosition(0);
        text.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return text;
    }

    public void show() {
        if (!this.processOptions()) {
            return;
        }
        this.dialog = DialogUtils.dialog("Preview", 640, 480);
        this.dialog.setLayout(new BorderLayout());
        JTabbedPane pain = new JTabbedPane();
        pain.addTab("Raw", this.buildRaw());
        pain.addTab("HTML", this.buildHTML());
        pain.addTab("Text", new JScrollPane(this.buildText()));
        JButton closeme = new JButton("Close");
        closeme.addActionListener(this);
        this.dialog.add(pain, "Center");
        this.dialog.add(DialogUtils.center(closeme), "South");
        this.dialog.setVisible(true);
        this.dialog.show();
    }

}

