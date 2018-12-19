package proxy;

import ssl.SecureProxySocket;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

public class HTTPProxy implements Runnable {
    protected String server;
    protected int port;
    protected int sport;
    protected List listeners = new LinkedList();
    protected ServerSocket pserver;
    protected boolean alive = true;
    protected long requests = 0L;
    protected long rx = 0L;
    protected long fails = 0L;

    public void addProxyListener(HTTPProxyEventListener l) {
        this.listeners.add(l);
    }

    public void fireEvent(int type, String text) {
        for (Object listener : this.listeners) {
            ((HTTPProxyEventListener) listener).proxyEvent(type, text);
        }
    }

    public void stop() {
        this.alive = false;
        try {
            this.pserver.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public HTTPProxy(int sport, String server, int port) throws IOException {
        this.server = server;
        this.port = port;
        this.sport = sport;
        this.pserver = new ServerSocket(sport, 128);
    }

    public int getPort() {
        return this.port;
    }

    public void start() {
        new Thread(this).start();
    }

    private static int checkLen(String t, int d, StringBuilder buffer) {
        buffer.append(t).append("\r\n");
        if (t.startsWith("Content-Length: ")) {
            try {
                return Integer.parseInt(t.substring(16).trim());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return d;
    }

    private static String readLine(DataInputStream in) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int read2;
        while ((char) (read2 = in.readUnsignedByte()) != '\n') {
            if ((char) read2 == '\r') {
                continue;
            }
            buffer.append((char) read2);
        }
        return buffer.toString();
    }

    @Override
    public void run() {
        try {
            while (this.alive) {
                Socket client = this.pserver.accept();
                client.setSoTimeout(60000);
                new ProxyClient(client);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class ProxyClient implements Runnable {
        protected Socket socket;
        protected Socket proxy = null;

        public ProxyClient(Socket socket) {
            this.socket = socket;
            new Thread(this).start();
        }

        @Override
        public void run() {
            String trustme = "";
            DataOutputStream out = null;
            boolean sentheaders = false;
            String request = "";
            try {
                this.proxy = new Socket(HTTPProxy.this.server, HTTPProxy.this.port);
                StringBuilder buffer = new StringBuilder(8192);
                InputStream is = this.socket.getInputStream();
                DataInputStream in = new DataInputStream(is);
                out = new DataOutputStream(this.socket.getOutputStream());
                request = HTTPProxy.readLine(in);
                buffer.append(request).append("\r\n");
                int len = 0;
                String temp = HTTPProxy.readLine(in);
                len = HTTPProxy.checkLen(temp, len, buffer);
                while (temp.length() > 0) {
                    temp = HTTPProxy.readLine(in);
                    len = HTTPProxy.checkLen(temp, len, buffer);
                }
                if (request.startsWith("CONNECT")) {
                    out.writeBytes("HTTP/1.1 200 Connection established\r\n\r\n");
                    String[] items = request.split(" ");
                    String host = items[1];
                    if (host.endsWith(":443")) {
                        host = host.substring(0, host.length() - 4);
                    }
                    trustme = host;
                    buffer = new StringBuilder(8192);
                    this.socket = new SecureProxySocket(this.socket).getSocket();
                    in = new DataInputStream(this.socket.getInputStream());
                    out = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
                    request = HTTPProxy.readLine(in);
                    if (request.startsWith("GET ")) {
                        buffer.append("GET https://").append(host).append(request.substring(4)).append("\r\n");
                    } else if (request.startsWith("POST ")) {
                        buffer.append("POST https://").append(host).append(request.substring(5)).append("\r\n");
                    }
                    len = 0;
                    temp = HTTPProxy.readLine(in);
                    len = HTTPProxy.checkLen(temp, len, buffer);
                    while (temp.length() > 0) {
                        temp = HTTPProxy.readLine(in);
                        len = HTTPProxy.checkLen(temp, len, buffer);
                    }
                }
                DataOutputStream outz = new DataOutputStream(new BufferedOutputStream(this.proxy.getOutputStream()));
                outz.writeBytes(buffer.toString());
                outz.flush();
                if (len > 0) {
                    byte[] big = new byte[len];
                    int read2;
                    while (len > 0) {
                        read2 = in.read(big);
                        outz.write(big, 0, read2);
                        outz.flush();
                        len -= read2;
                    }
                }
                DataInputStream inz = new DataInputStream(this.proxy.getInputStream());
                buffer = new StringBuilder(8192);
                temp = HTTPProxy.readLine(inz);
                len = 0;
                len = HTTPProxy.checkLen(temp, len, buffer);
                while (temp.length() > 0) {
                    temp = HTTPProxy.readLine(inz);
                    len = HTTPProxy.checkLen(temp, len, buffer);
                }
                HTTPProxy.this.rx += (long) len;
                if (len == 0) {
                    out.writeBytes(buffer.toString());
                    out.flush();
                    sentheaders = true;
                } else {
                    byte[] big = new byte[len];
                    int read3;
                    int fail = 0;
                    int rd = 0;
                    while (len > 0) {
                        read3 = inz.read(big);
                        if (read3 > 0) {
                            if (!sentheaders) {
                                out.writeBytes(buffer.toString());
                                out.flush();
                                sentheaders = true;
                            }
                            out.write(big, 0, read3);
                            out.flush();
                            len -= read3;
                            rd += read3;
                            continue;
                        }
                        throw new IOException("incomplete read " + fail + ", need: " + len + " bytes, read: " + rd + " bytes");
                    }
                }
                ++HTTPProxy.this.requests;
            } catch (SSLHandshakeException sex) {
                HTTPProxy.this.fireEvent(0, "add to trusted hosts: " + trustme);
                ++HTTPProxy.this.fails;
                sentheaders = true;
            } catch (SocketException soex) {
                HTTPProxy.this.fireEvent(1, "browser proxy refused connection.");
                ++HTTPProxy.this.fails;
            } catch (Exception ioex) {
                ++HTTPProxy.this.fails;
            } finally {
                try {
                    try {
                        if (!sentheaders && out != null && !request.startsWith("CONNECT") && (request.trim() + "").length() > 0) {
                            String[] parts = request.split(" ");
                            out.writeBytes("HTTP/1.1 302\r\nLocation: " + parts[1] + "\r\n\r\n");
                            out.flush();
                        }
                    } catch (Exception ex) {
                    }
                    if (this.socket != null) {
                        this.socket.close();
                    }
                    if (this.proxy != null) {
                        this.proxy.close();
                    }
                } catch (Exception eex) {
                }
            }
            HTTPProxy.this.fireEvent(3, HTTPProxy.this.requests + " " + HTTPProxy.this.fails + " " + HTTPProxy.this.rx);
        }
    }

}

