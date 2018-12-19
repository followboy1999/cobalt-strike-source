package ssl;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;

public class SecureProxySocket {
    protected SSLSocket socket;
    private static byte[] buffer = null;

    public static SSLSocketFactory getMyFactory() throws Exception {
        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(null, new TrustManager[]{new ITrustYouDude()}, new SecureRandom());
        return sslcontext.getSocketFactory();
    }

    public SecureProxySocket(String host, int port) throws Exception {
        SSLSocketFactory factory = SecureProxySocket.getMyFactory();
        this.socket = (SSLSocket) factory.createSocket(host, port);
        this.socket.setSoTimeout(4048);
        this.socket.startHandshake();
    }

    public SecureProxySocket(Socket old_socket) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksIs = this.getClass().getClassLoader().getResourceAsStream("resources/proxy.store");
        ks.load(ksIs, "123456".toCharArray());
        ksIs.close();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "123456".toCharArray());
        SSLContext sslcontext = SSLContext.getInstance("SSL");
        sslcontext.init(kmf.getKeyManagers(), new TrustManager[]{new ITrustYouDude()}, new SecureRandom());
        SSLSocketFactory factory = sslcontext.getSocketFactory();
        this.socket = (SSLSocket) factory.createSocket(old_socket, old_socket.getInetAddress().getHostName(), old_socket.getPort(), true);
        this.socket.setUseClientMode(false);
        this.socket.setSoTimeout(8192);
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

    public Socket getSocket() {
        return this.socket;
    }
}

