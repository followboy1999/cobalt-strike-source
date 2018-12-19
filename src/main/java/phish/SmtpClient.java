package phish;

import common.AObject;
import common.CommonUtils;
import encoders.Base64;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@SuppressWarnings("ALL")
public class SmtpClient
        extends AObject implements ArmitageTrustListener {
    protected Socket socket = null;
    protected InputStream in = null;
    protected OutputStream out = null;
    protected SmtpNotify notify;

    public SmtpClient(SmtpNotify notify) {
        this.notify = notify;
    }

    @Override
    public boolean trust(String fingerprint) {
        return true;
    }

    public void update(String message) {
        if (this.notify != null) {
            this.notify.update(message);
        }
    }

    public String readLoop(InputStream in) throws IOException {
        String temp = CommonUtils.bString(SecureSocket.readbytes(in));
        this.checkSmtpError(temp);
        return temp;
    }

    public void checkSmtpError(String r) {
        if (!r.startsWith("2") && !r.startsWith("3")) {
            throw new RuntimeException(r);
        }
    }

    public void writeb(OutputStream out, String data) throws IOException {
        for (int x = 0; x < data.length(); ++x) {
            out.write((byte) data.charAt(x));
        }
        out.flush();
    }

    public String send_email(String serverspec, String bounce, String to, String message) throws Exception {
        SecureSocket ssocket;
        MailServer server = PhishingUtils.parseServerString(serverspec);
        String r;
        String domain = bounce.split("@")[1];
        if (server.delay > 0) {
            for (int delay = CommonUtils.rand(server.delay) + 1; delay > 0; --delay) {
                this.update("[Delay " + delay + "s]");
                CommonUtils.sleep(1000L);
            }
        }
        this.update("[Connecting to " + server.lhost + ":" + server.lport + "]");
        if (server.ssl) {
            SecureSocket ssocket2 = new SecureSocket(server.lhost, server.lport, this);
            this.socket = ssocket2.getSocket();
        } else {
            this.socket = new Socket(server.lhost, server.lport);
        }
        this.in = this.socket.getInputStream();
        this.out = new BufferedOutputStream(this.socket.getOutputStream(), 65536);
        this.socket.setSoTimeout(0);
        this.update("[Connected to " + server.lhost + ":" + server.lport + "]");
        r = this.readLoop(this.in);
        this.writeb(this.out, "EHLO " + domain + "\r\n");
        this.update("[EHLO " + domain + "]");
        r = this.readLoop(this.in);
        if (server.starttls) {
            this.writeb(this.out, "STARTTLS\r\n");
            this.update("[STARTTLS]");
            r = this.readLoop(this.in);
            ssocket = new SecureSocket(this.socket);
            this.socket = ssocket.getSocket();
            this.in = this.socket.getInputStream();
            this.out = new BufferedOutputStream(this.socket.getOutputStream(), 65536);
            this.socket.setSoTimeout(0);
            this.writeb(this.out, "EHLO " + domain + "\r\n");
            this.update("EHLO " + domain);
            r = this.readLoop(this.in);
        }
        if (server.username != null && server.password != null) {
            if (!server.starttls && CommonUtils.isin("STARTTLS", r) && !CommonUtils.isin("AUTH", r)) {
                this.writeb(this.out, "STARTTLS\r\n");
                this.update("[STARTTLS]");
                r = this.readLoop(this.in);
                ssocket = new SecureSocket(this.socket);
                this.socket = ssocket.getSocket();
                this.in = this.socket.getInputStream();
                this.out = new BufferedOutputStream(this.socket.getOutputStream(), 65536);
                this.socket.setSoTimeout(0);
                this.writeb(this.out, "EHLO " + domain + "\r\n");
                this.update("EHLO " + domain);
                r = this.readLoop(this.in);
            }
            this.writeb(this.out, "AUTH LOGIN\r\n");
            this.update("[AUTH LOGIN]");
            //noinspection UnusedAssignment
            r = this.readLoop(this.in);
            this.writeb(this.out, Base64.encode(server.username) + "\r\n");
            r = this.readLoop(this.in);
            this.writeb(this.out, Base64.encode(server.password) + "\r\n");
            r = this.readLoop(this.in);
            this.update("[I am authenticated...]");
        }
        this.writeb(this.out, "MAIL FROM: <" + bounce + ">\r\n");
        this.update("[MAIL FROM: <" + bounce + ">]");
        r = this.readLoop(this.in);
        this.writeb(this.out, "RCPT TO: <" + to + ">\r\n");
        this.update("[RCPT TO: <" + to + ">]");
        r = this.readLoop(this.in);
        this.writeb(this.out, "DATA\r\n");
        this.update("[DATA]");
        r = this.readLoop(this.in);
        this.writeb(this.out, message);
        this.writeb(this.out, "\r\n.\r\n");
        this.update("[Message Transmitted]");
        r = this.readLoop(this.in);
        return r;
    }

    public void cleanup() {
        try {
            if (this.in != null) {
                this.in.close();
            }
            if (this.out != null) {
                this.out.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
            this.in = null;
            this.out = null;
            this.socket = null;
        } catch (IOException ioex) {
            // empty catch block
        }
    }
}

