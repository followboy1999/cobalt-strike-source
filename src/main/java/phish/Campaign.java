package phish;

import common.*;
import dialog.DialogUtils;
import mail.Eater;
import server.ManageUser;
import server.Phisher;
import server.Resources;
import server.ServerUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Campaign
        extends AObject implements Runnable,
        SmtpNotify {
    protected Request request;
    protected ManageUser client;
    protected Resources resources;
    protected Map options;
    protected Phisher phisher;
    protected String templated;
    protected String sid;
    protected boolean keepgoing = true;

    public Campaign(Phisher p, Request r, ManageUser c, Resources r2) {
        this.phisher = p;
        this.request = r;
        this.client = c;
        this.resources = r2;
        this.sid = (String) r.arg(0);
        this.templated = (String) r.arg(1);
        this.options = (Map) r.arg(2);
        new Thread(this, "Phishing Campaign").start();
    }

    @Override
    public void update(String message) {
        this.resources.send(this.client, "phishstatus." + this.sid, message);
    }

    public void cancel() {
        this.keepgoing = false;
    }

    @Override
    public void run() {
        try {
            String attachmentr = DialogUtils.string(this.options, "attachmentr");
            String templatef = DialogUtils.string(this.options, "template");
            String bounce = DialogUtils.string(this.options, "bounce");
            String server = DialogUtils.string(this.options, "server");
            String url = DialogUtils.string(this.options, "url");
            List contacts = (List) this.options.get("targets");
            PhishEvents events = new PhishEvents(this.sid);
            Eater template = new Eater(new ByteArrayInputStream(CommonUtils.toBytes(this.templated)));
            String subject = template.getSubject();
            if (attachmentr != null && !"".equals(attachmentr) && new File(attachmentr).exists()) {
                template.attachFile(attachmentr);
            }
            this.resources.sendAndProcess(this.client, "phishlog." + this.sid, events.SendmailStart(contacts.size(), attachmentr, bounce, server, subject, templatef, url));
            Iterator i = contacts.iterator();
            while (i.hasNext() && this.keepgoing) {
                Map next = (Map) i.next();
                String to = next.get("To") + "";
                String toName = next.get("To_Name") + "";
                String token = CommonUtils.ID().substring(24, 36);
                ServerUtils.addToken(this.resources, token, to, this.sid);
                SmtpClient mailer = new SmtpClient(this);
                try {
                    this.resources.sendAndProcess(this.client, "phishlog." + this.sid, events.SendmailPre(to));
                    String message = CommonUtils.bString(template.getMessage(null, "".equals(toName) ? to : toName + " <" + to + ">"));
                    message = PhishingUtils.updateMessage(message, next, url, token);
                    String status = mailer.send_email(server, bounce, to, message);
                    this.resources.sendAndProcess(this.client, "phishlog." + this.sid, events.SendmailPost(to, "SUCCESS", status, token));
                } catch (Exception ex) {
                    MudgeSanity.logException("phish to " + to + " via " + server, ex, false);
                    this.resources.sendAndProcess(this.client, "phishlog." + this.sid, events.SendmailPost(to, "Failed", ex.getMessage(), token));
                }
                mailer.cleanup();
                this.update("");
            }
            this.resources.sendAndProcess(this.client, "phishlog." + this.sid, events.SendmailDone());
            this.resources.call("tokens.push");
        } catch (Exception ex) {
            MudgeSanity.logException("Campaign", ex, false);
        }
    }
}

