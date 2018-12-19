package ssl;

import common.CommonUtils;
import common.MudgeSanity;
import sleep.bridges.io.IOObject;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class SecureServerSocket {
    protected ServerSocket server;

    public IOObject accept() {
        try {
            Socket client = this.server.accept();
            IOObject temp = new IOObject();
            temp.openRead(client.getInputStream());
            temp.openWrite(new BufferedOutputStream(client.getOutputStream(), 65536));
            client.setSoTimeout(0);
            return temp;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected boolean authenticate(Socket client, String pass, String host) throws IOException {
        int x;
        DataInputStream authin = new DataInputStream(client.getInputStream());
        DataOutputStream authout = new DataOutputStream(client.getOutputStream());
        int magic = authin.readInt();
        if (magic != 48879) {
            CommonUtils.print_error("rejected client from " + host + ": invalid auth protocol (old client?)");
            return false;
        }
        int length = authin.readUnsignedByte();
        if (length <= 0) {
            CommonUtils.print_error("rejected client from " + host + ": bad password length");
            return false;
        }
        StringBuilder mypass = new StringBuilder();
        for (x = 0; x < length; ++x) {
            mypass.append((char) authin.readUnsignedByte());
        }
        for (x = length; x < 256; ++x) {
            authin.readUnsignedByte();
        }
        Class<?> x2 = this.getClass();
        synchronized (x2) {
            CommonUtils.sleep(CommonUtils.rand(1000));
        }
        if (mypass.toString().equals(pass)) {
            authout.writeInt(51966);
            return true;
        }
        authout.writeInt(0);
        CommonUtils.print_error("rejected client from " + host + ": invalid password");
        return false;
    }

    public Socket acceptAndAuthenticate(final String pass, final PostAuthentication next) {
        String last = "unknown";
        try {
            final Socket client = this.server.accept();
            last = client.getInetAddress().getHostAddress();
            new Thread(() -> {
                String last1 = "unknown";
                try {
                    last1 = client.getInetAddress().getHostAddress();
                    if (SecureServerSocket.this.authenticate(client, pass, last1)) {
                        next.clientAuthenticated(client);
                        return;
                    }
                } catch (Exception ex) {
                    MudgeSanity.logException("could not authenticate client from " + last1, ex, false);
                }
                try {
                    if (client != null) {
                        client.close();
                    }
                } catch (Exception ex) {
                    // empty catch block
                }
            }, "accept client from " + last + " (auth phase)").start();
        } catch (Exception ex) {
            MudgeSanity.logException("could not accept client from " + last, ex, false);
        }
        return null;
    }

    public SecureServerSocket(int port) throws Exception {
        ServerSocketFactory factory = this.getFactory();
        this.server = factory.createServerSocket(port, 32);
        this.server.setSoTimeout(0);
        this.server.setReuseAddress(true);
    }

    private ServerSocketFactory getFactory() {
        return SSLServerSocketFactory.getDefault();
    }

    public ServerSocket getServerSocket() {
        return this.server;
    }

    public String fingerprint() {
        try {
            FileInputStream is = new FileInputStream(System.getProperty("javax.net.ssl.keyStore"));
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, (System.getProperty("javax.net.ssl.keyStorePassword") + "").toCharArray());
            Enumeration<String> en = keystore.aliases();
            if (en.hasMoreElements()) {
                String alias = en.nextElement() + "";
                Certificate cert = keystore.getCertificate(alias);
                byte[] bytesOfMessage = cert.getEncoded();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] thedigest = md.digest(bytesOfMessage);
                BigInteger bi = new BigInteger(1, thedigest);
                return bi.toString(16);
            }
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
        return "unknown";
    }

}

