package common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PhishEvents {
    protected String sid;

    public PhishEvents(String sid) {
        this.sid = sid;
    }

    protected PhishEvent build(String name, LinkedList items, String desc, Map info) {
        return new PhishEvent(this.sid, name, items, desc, info);
    }

    public PhishEvent SendmailStart(int ntarget, String attachment, String bounceto, String server, String subject, String templatef, String url) {
        StringBuilder desc = new StringBuilder();
        desc.append("[Campaign Start]\n");
        desc.append("Number of targets: ").append(ntarget).append("\n");
        desc.append("Template:          ").append(templatef).append("\n");
        desc.append("Subject:           ").append(subject).append("\n");
        desc.append("URL:               ").append(url).append("\n");
        desc.append("Attachment:        ").append(ntarget).append("\n");
        desc.append("Mail Server:       ").append(server).append("\n");
        desc.append("Bounce To:         ").append(bounceto).append("\n");
        LinkedList<Object> items = new LinkedList<>();
        items.add(this.sid);
        items.add((long) ntarget);
        items.add(attachment);
        items.add(bounceto);
        items.add(server);
        items.add(subject);
        items.add(templatef);
        items.add(url);
        HashMap<String, Object> info = new HashMap<>();
        info.put("when", System.currentTimeMillis());
        info.put("type", "sendmail_start");
        info.put("subject", subject);
        info.put("url", url);
        info.put("attachment", attachment);
        info.put("template", templatef);
        info.put("subject", subject);
        info.put("cid", this.sid);
        return this.build("sendmail_start", items, desc.toString(), info);
    }

    public PhishEvent SendmailPre(String email) {
        LinkedList<String> items = new LinkedList<>();
        items.add(this.sid);
        items.add(email);
        return this.build("sendmail_pre", items, "[Send] " + email, null);
    }

    public PhishEvent SendmailPost(String email, String status, String other, String token) {
        LinkedList<String> items = new LinkedList<>();
        items.add(this.sid);
        items.add(email);
        items.add(status);
        items.add(other);
        HashMap<String, Object> info = new HashMap<>();
        info.put("when", System.currentTimeMillis());
        info.put("type", "sendmail_post");
        info.put("status", status);
        info.put("data", other.trim());
        info.put("token", token);
        info.put("cid", this.sid);
        return this.build("sendmail_post", items, "[Status] " + token + " " + email + " " + status + " " + other.trim(), info);
    }

    public PhishEvent SendmailDone() {
        LinkedList<String> items = new LinkedList<>();
        items.add(this.sid);
        return this.build("sendmail_done", items, "[Campaign Complete]", null);
    }
}

