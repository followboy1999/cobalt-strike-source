package server;

import common.CommonUtils;
import common.MudgeSanity;
import common.Request;
import common.StringStack;
import ssl.ArmitageTrustListener;
import ssl.SecureSocket;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebsiteCloneTool implements Runnable,
        HostnameVerifier,
        ArmitageTrustListener {
    protected Request request;
    protected ManageUser client;

    public WebsiteCloneTool(Request r, ManageUser c) {
        this.request = r;
        this.client = c;
        new Thread(this, "Clone: " + r.arg(0)).start();
    }

    @Override
    public boolean trust(String fingerprint) {
        return true;
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        return true;
    }

    private String cloneAttempt(String url) throws Exception {
        String base;
        URL myurl = new URL(url);
        HttpURLConnection yc = (HttpURLConnection) myurl.openConnection();
        if (yc instanceof HttpsURLConnection) {
            HttpsURLConnection yc_secure = (HttpsURLConnection) yc;
            yc_secure.setHostnameVerifier(this);
            yc_secure.setSSLSocketFactory(SecureSocket.getMyFactory(this));
        }
        yc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
        yc.setInstanceFollowRedirects(true);
        byte[] data = CommonUtils.readAll(yc.getInputStream());
        if (yc.getResponseCode() == 302 || yc.getResponseCode() == 301) {
            return this.cloneAttempt(yc.getHeaderField("location"));
        }
        String text = CommonUtils.bString(data);
        if (!myurl.getFile().endsWith("/")) {
            StringStack parts = new StringStack(myurl.getFile(), "/");
            parts.pop();
            base = CommonUtils.strrep(url, myurl.getFile(), parts.toString() + "/");
        } else {
            base = url;
        }
        if (!text.toLowerCase().contains("shortcut icon") && !text.toLowerCase().contains("rel=\"icon")) {
            text = text.replaceFirst("(?i:<head.*?>)", "$0\n<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"/favicon.ico\">");
        }
        if (!text.toLowerCase().contains("<base href=")) {
            text = text.replaceFirst("(?i:<head.*?>)", "$0\n<base href=\"" + base + "\">");
        }
        return text;
    }

    @Override
    public void run() {
        String url = this.request.arg(0) + "";
        try {
            String data = this.cloneAttempt(url);
            this.client.write(this.request.reply(data));
        } catch (Exception iex) {
            MudgeSanity.logException("clone: " + url, iex, false);
            this.client.write(this.request.reply("error: " + iex.getMessage()));
        }
    }
}

