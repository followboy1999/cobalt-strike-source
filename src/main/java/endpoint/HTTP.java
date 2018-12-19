package endpoint;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;
import tap.EncryptedTap;
import tap.TapProtocol;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class HTTP
        extends Base implements WebService {
    protected byte[] buffer = new byte[1048576];
    protected ByteArrayOutputStream outframes = new ByteArrayOutputStream(1048576);
    protected DataOutputStream outhandle = new DataOutputStream(this.outframes);
    protected WebServer server = null;
    protected String hook = "";

    public HTTP(TapProtocol tapProtocol) {
        super(tapProtocol);
        this.start();
    }

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        EncryptedTap encryptedTap = new EncryptedTap("phear0", "foobar".getBytes());
        try {
            WebServer webServer = new WebServer(80);
            HTTP hTTP = new HTTP(encryptedTap);
            hTTP.setup(webServer, ".json");
            do {
                Thread.sleep(1000L);
            } while (true);
        } catch (InterruptedException interruptedException) {
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    @Override
    public synchronized void processFrame(byte[] arrby) {
        try {
            this.outhandle.writeShort(arrby.length);
            this.outhandle.write(arrby, 0, arrby.length);
        } catch (IOException iOException) {
            System.err.println("Size: " + arrby.length);
            iOException.printStackTrace();
            this.stop();
        }
    }

    @Override
    public void setup(WebServer webServer, String string) {
        webServer.register("/send" + string, this);
        webServer.register("/receive" + string, this);
        this.server = webServer;
        this.hook = string;
    }

    @Override
    public void shutdown() {
        this.server.deregister("/send" + this.hook);
        this.server.deregister("/receive" + this.hook);
    }

    @Override
    public Response serve(String string, String string2, Properties properties, Properties properties2) {
        if (string.startsWith("/send") && properties2.containsKey("input") && properties2.get("input") instanceof InputStream && properties2.containsKey("length") && properties2.get("length") instanceof Long) {
            try {
                int n;
                long l2 = (Long) properties2.get("length");
                DataInputStream dataInputStream = new DataInputStream((InputStream) properties2.get("input"));
                int n2 = 0;
                while ((long) n2 < l2 && (long) (n2 + (n = dataInputStream.readUnsignedShort())) <= l2) {
                    n2 += n + 2;
                    dataInputStream.readFully(this.buffer, 0, n);
                    this.rx += (long) n;
                    this.tap.writeFrame(this.buffer, n);
                }
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            return new Response("200 OK", "application/json", "{ \"status\": \"OK\" }");
        }
        if (string.startsWith("/receive")) {
            this.tap.setRemoteHost((properties.get("REMOTE_ADDRESS") + "").substring(1));
            ByteArrayInputStream byteArrayInputStream;
            synchronized (this) {
                byteArrayInputStream = new ByteArrayInputStream(this.outframes.toByteArray());
                this.outframes.reset();
            }
            return new Response("200 OK", "application/octet-stream", byteArrayInputStream);
        }
        return new Response("200 OK", "text/plain", "file not found");
    }

    public String toString() {
        return "tunnels " + this.tap.getInterface();
    }

    @Override
    public String getType() {
        return "tunnel";
    }

    @Override
    public List cleanupJobs() {
        return new LinkedList();
    }

    @Override
    public boolean suppressEvent(String string) {
        return true;
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }
}

