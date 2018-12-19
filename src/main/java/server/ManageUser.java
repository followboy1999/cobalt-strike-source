package server;

import aggressor.Aggressor;
import common.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ManageUser implements Runnable {
    protected TeamSocket client;
    protected boolean authenticated = false;
    protected String nickname = "";
    protected Resources resources;
    protected BroadcastWriter writer = null;
    protected Map calls;
    protected Thread mine = null;

    public ManageUser(TeamSocket c, Resources r, Map l) {
        this.client = c;
        this.resources = r;
        this.calls = l;
    }

    public boolean isConnected() {
        return this.client.isConnected();
    }

    public String getNick() {
        return this.nickname;
    }

    public void write(Reply r) {
        this.writer.addReply(r);
    }

    public void writeNow(Reply r) {
        if (Thread.currentThread() != this.mine) {
            CommonUtils.print_error("writeNow " + r + " should be called in: " + this.mine + " not: " + Thread.currentThread());
            this.write(r);
        } else {
            this.client.writeObject(r);
        }
    }

    public void process(Request r) throws Exception {
        if (!this.authenticated && "aggressor.authenticate".equals(r.getCall()) && r.size() == 3) {
            String user = r.arg(0) + "";
            String pass = r.arg(1) + "";
            String ver = r.arg(2) + "";
            if (!Aggressor.VERSION.equals(ver)) {
                this.client.writeObject(r.reply("Your client software does not match this server\nClient: " + ver + "\nServer: " + Aggressor.VERSION));
            } else if (ServerUtils.getServerPassword(this.resources, user).equals(pass)) {
                if (this.resources.isRegistered(user)) {
                    this.client.writeObject(r.reply("User is already connected."));
                } else {
                    this.client.writeObject(r.reply("SUCCESS"));
                    this.authenticated = true;
                    this.nickname = user;
                    Thread.currentThread().setName("Manage: " + this.nickname);
                    this.writer = new BroadcastWriter();
                    new Thread(this.writer, "Writer for: " + this.nickname).start();
                }
            } else {
                this.client.writeObject(r.reply("Logon failure"));
            }
        } else if (!this.authenticated) {
            this.client.close();
        } else if ("aggressor.metadata".equals(r.getCall()) && r.size() == 1) {
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("nick", this.nickname);
            ServerUtils.getProfile(this.resources).getPreview().summarize(metadata);
            long time = System.currentTimeMillis() - Long.parseLong(r.arg(0) + "");
            metadata.put("clockskew", time);
            metadata.put("signer", ServerUtils.getProfile(this.resources).getCodeSigner());
            metadata.put("validssl", ServerUtils.getProfile(this.resources).hasValidSSL() ? "true" : "false");
            this.client.writeObject(r.reply(metadata));
        } else if ("aggressor.ready".equals(r.getCall())) {
            this.resources.register(this.nickname, this);
            this.resources.broadcast("eventlog", LoggedEvent.Join(this.nickname));
        } else if ("aggressor.ping".equals(r.getCall()) && r.size() == 1) {
            this.client.writeObject(r.reply(r.arg(0)));
        } else if ("aggressor.users".equals(r.getCall())) {
            this.client.writeObject(r.reply(this.resources.getUsers()));
        } else if ("aggressor.event".equals(r.getCall()) && r.size() == 1) {
            LoggedEvent ev = (LoggedEvent) r.arg(0);
            ev.touch();
            if (ev.type == 1) {
                if (this.resources.isRegistered(ev.to)) {
                    if (ev.from.equals(ev.to)) {
                        this.resources.send(ev.from, "eventlog", ev);
                    } else {
                        this.resources.send(ev.from, "eventlog", ev);
                        this.resources.send(ev.to, "eventlog", ev);
                    }
                } else {
                    this.resources.send(ev.from, "eventlog", LoggedEvent.NoUser(ev));
                }
            } else {
                this.resources.broadcast("eventlog", ev);
            }
        } else if ("armitage.upload".equals(r.getCall()) && r.size() == 1) {
            File file = CommonUtils.SafeFile("uploads", r.arg(0) + "");
            file.mkdirs();
            file.delete();
            this.client.writeObject(r.reply(file.getAbsolutePath()));
        } else if ("aggressor.resource".equals(r.getCall()) && r.size() == 1) {
            String file = (String) r.arg(0);
            if ("winvnc.x86.dll".equals(file)) {
                this.client.writeObject(r.reply(CommonUtils.readFile("third-party/winvnc.x86.dll")));
            } else if ("winvnc.x64.dll".equals(file)) {
                this.client.writeObject(r.reply(CommonUtils.readFile("third-party/winvnc.x64.dll")));
            }
        } else if ("aggressor.sysinfo".equals(r.getCall())) {
            this.client.writeObject(r.reply(MudgeSanity.systemInformation()));
        } else if (r.is("aggressor.screenshot", 1)) {
            TabScreenshot screen = (TabScreenshot) r.arg(0);
            screen.touch(this.nickname);
            this.resources.process(screen);
        } else if ("armitage.append".equals(r.getCall()) && r.size() == 2) {
            File file = CommonUtils.SafeFile("uploads", r.arg(0) + "");
            byte[] data = (byte[]) r.arg(1);
            try {
                FileOutputStream out = new FileOutputStream(file, true);
                out.write(data);
                out.close();
                this.client.writeObject(r.reply(file.getAbsolutePath()));
            } catch (IOException ioex) {
                this.client.writeObject(r.reply("ERROR: " + ioex.getMessage()));
                MudgeSanity.logException(r.getCall() + " " + file, ioex, true);
            }
        } else if ("armitage.broadcast".equals(r.getCall()) && r.size() == 2) {
            String key = (String) r.arg(0);
            Object value = r.arg(1);
            this.resources.broadcast(key, value, true);
        } else if ("aggressor.reset_data".equals(r.getCall()) && r.size() == 0) {
            CommonUtils.print_warn(this.getNick() + " reset the data model.");
            this.resources.reset();
        } else if (this.calls.containsKey(r.getCall())) {
            ServerHook callme = (ServerHook) this.calls.get(r.getCall());
            callme.call(r, this);
        } else {
            this.client.writeObject(new Reply("server_error", 0L, r + ": unknown call [or bad arguments]"));
        }
    }

    @Override
    public void run() {
        try {
            this.mine = Thread.currentThread();
            while (this.client.isConnected()) {
                Request r = (Request) this.client.readObject();
                if (r == null) continue;
                this.process(r);
            }
        } catch (Exception ex) {
            MudgeSanity.logException("manage user", ex, false);
            this.client.close();
        }
        if (this.authenticated) {
            this.resources.deregister(this.nickname, this);
            this.resources.broadcast("eventlog", LoggedEvent.Quit(this.nickname));
        }
    }

    private class BroadcastWriter implements Runnable {
        protected LinkedList<Reply> replies = new LinkedList<>();

        protected Reply grabReply() {
            synchronized (this) {
                return this.replies.pollFirst();
            }
        }

        protected void addReply(Reply r) {
            synchronized (this) {
                if (this.replies.size() > 100000) {
                    this.replies.removeFirst();
                }
                this.replies.add(r);
            }
        }

        @Override
        public void run() {
            try {
                while (ManageUser.this.client.isConnected()) {
                    Reply next = this.grabReply();
                    if (next != null) {
                        ManageUser.this.client.writeObject(next);
                        Thread.yield();
                        continue;
                    }
                    Thread.sleep(25L);
                }
            } catch (Exception ex) {
                MudgeSanity.logException("bwriter", ex, false);
            }
        }
    }

}

