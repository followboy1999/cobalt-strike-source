package ssl;

import sleep.bridges.io.IOObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;

public class SecureSocket {
    protected SSLSocket socket;
    private static byte[] buffer = null;

    public static SSLSocketFactory getMyFactory(ArmitageTrustListener checker) throws Exception {
        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, new TrustManager[]{new ArmitageTrustManager(checker)}, new SecureRandom());
        return sslcontext.getSocketFactory();
    }

    public SecureSocket(String host, int port, ArmitageTrustListener checker) throws Exception {
        SSLSocketFactory factory = SecureSocket.getMyFactory(checker);
        this.socket = (SSLSocket) factory.createSocket(host, port);
        this.socket.setSoTimeout(4048);
        this.socket.startHandshake();
    }

    public SecureSocket(Socket old_socket) throws Exception {
        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, new TrustManager[]{new ITrustYouDude()}, new SecureRandom());
        SSLSocketFactory factory = sslcontext.getSocketFactory();
        this.socket = (SSLSocket) factory.createSocket(old_socket, old_socket.getInetAddress().getHostName(), old_socket.getPort(), true);
        this.socket.setUseClientMode(true);
        this.socket.setSoTimeout(4048);
        this.socket.startHandshake();
    }

    public static byte[] readbytes(InputStream stream) throws IOException {
        Class<SecureSocket> class_ = SecureSocket.class;
        synchronized (SecureSocket.class) {
            int length;
            if (buffer == null) {
                buffer = new byte[1048576];
            }
            if ((length = stream.read(buffer)) > 0) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return Arrays.copyOf(buffer, length);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return new byte[0];
        }
    }

    public void authenticate(String password) {
        try {
            int x;
            this.socket.setSoTimeout(0);
            DataInputStream datain = new DataInputStream(this.socket.getInputStream());
            DataOutputStream dataout = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            dataout.writeInt(48879);
            dataout.writeByte(password.length());
            for (x = 0; x < password.length(); ++x) {
                dataout.writeByte((byte) password.charAt(x));
            }
            for (x = password.length(); x < 256; ++x) {
                dataout.writeByte(65);
            }
            dataout.flush();
            int result = datain.readInt();
            if (result == 51966) {
                return;
            }
            throw new RuntimeException("authentication failure!");
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public IOObject client() {
        try {
            IOObject temp = new IOObject();
            temp.openRead(this.socket.getInputStream());
            temp.openWrite(new BufferedOutputStream(this.socket.getOutputStream(), 65536));
            this.socket.setSoTimeout(0);
            return temp;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
}

