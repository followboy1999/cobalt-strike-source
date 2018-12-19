package cloudstrike;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

public class NanoHTTPD {
    public static final String HTTP_OK = "200 OK";
    public static final String HTTP_PARTIAL_CONTENT = "206 Partial Content";
    public static final String HTTP_MULTISTATE = "207 Multi-Status";
    public static final String HTTP_REDIRECT = "301 Moved Permanently";
    public static final String HTTP_NOT_MODIFIED = "304 Not Modified";
    public static final String HTTP_FORBIDDEN = "403 Forbidden";
    public static final String HTTP_NOTFOUND = "404 Not Found";
    public static final String HTTP_BADREQUEST = "400 Bad Request";
    public static final String HTTP_TOOLARGE = "413 Entity Too Large";
    public static final String HTTP_RANGE_NOT_SATISFIABLE = "416 Range Not Satisfiable";
    public static final String HTTP_INTERNALERROR = "500 Internal Server Error";
    public static final String HTTP_NOTIMPLEMENTED = "501 Not Implemented";
    public static final String MIME_PLAINTEXT = "text/plain";
    public static final String MIME_HTML = "text/html";
    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    protected boolean isssl = false;
    private ServerSocket ss = null;
    protected boolean alive = true;
    protected Thread fred;
    private int myTcpPort;
    private static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);

    public static void print_info(String message) {
        System.out.println("\u001b[01;34m[*]\u001b[0m " + message);
    }

    public static void print_warn(String message) {
        System.out.println("\u001b[01;33m[!]\u001b[0m " + message);
    }

    public static void print_error(String message) {
        System.out.println("\u001b[01;31m[-]\u001b[0m " + message);
    }

    public static void logException(String activity, Throwable ex, boolean expected) {
        if (expected) {
            NanoHTTPD.print_warn("Trapped " + ex.getClass().getName() + " during " + activity + " [" + Thread.currentThread().getName() + "]: " + ex.getMessage());
        } else {
            NanoHTTPD.print_error("Trapped " + ex.getClass().getName() + " during " + activity + " [" + Thread.currentThread().getName() + "]: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Response serve(String uri, String method, Properties header, Properties parms) {
        return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "This is the default!");
    }

    public NanoHTTPD(int port) throws IOException {
        this(port, false, null, null);
    }

    public boolean alwaysRaw(String uri) {
        return false;
    }

    public NanoHTTPD(int port, boolean ssl, InputStream keystore, String password) throws IOException {
        this.myTcpPort = port;
        this.listen(ssl, keystore, password);
    }

    public boolean isSSL() {
        return this.isssl;
    }

    public SSLServerSocketFactory getSSLFactory(InputStream ksIs, String password) {
        try {
            if (ksIs == null) {
                ksIs = this.getClass().getClassLoader().getResourceAsStream("resources/ssl.store");
                password = "123456";
                NanoHTTPD.print_warn("Web Server will use default SSL certificate (you don't want this).\n\tUse a valid SSL certificate with Cobalt Strike: https://www.cobaltstrike.com/help-malleable-c2#validssl");
            } else {
                NanoHTTPD.print_info("Web Server will use user-specified SSL certifcate");
            }
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(ksIs, password.toCharArray());
            ksIs.close();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(kmf.getKeyManagers(), new TrustManager[]{new TrustEverything()}, new SecureRandom());
            return sslcontext.getServerSocketFactory();
        } catch (Exception ex) {
            NanoHTTPD.logException("SSL certificate setup", ex, false);
            return null;
        }
    }

    public void listen(boolean ssl, InputStream keystore, String password) throws IOException {
        if (ssl) {
            this.isssl = true;
            SSLServerSocketFactory factory = this.getSSLFactory(keystore, password);
            this.ss = factory.createServerSocket(this.myTcpPort, 32);
            ((SSLServerSocket) this.ss).setEnabledCipherSuites(((SSLServerSocket) this.ss).getSupportedCipherSuites());
        } else {
            this.ss = new ServerSocket(this.myTcpPort);
        }
        Thread t = new Thread(() -> {
            try {
                NanoHTTPD.this.ss.setSoTimeout(500);
                while (NanoHTTPD.this.alive) {
                    try {
                        Socket temp = NanoHTTPD.this.ss.accept();
                        if (temp == null) continue;
                        new HTTPSession(temp);
                    } catch (SocketTimeoutException sex) {
                    }
                }
            } catch (IOException ioe) {
                // empty catch block
            }
            NanoHTTPD.print_info("Web Server on port " + NanoHTTPD.this.myTcpPort + " stopped");
        }, "Web Server on port " + this.myTcpPort);
        t.setDaemon(true);
        t.start();
        this.fred = t;
    }

    public void stop() {
        this.alive = false;
        this.fred.interrupt();
        try {
            this.ss.close();
        } catch (IOException ioex) {
            NanoHTTPD.logException("stop web server", ioex, false);
        }
    }

    @SuppressWarnings("deprecation")
    private String encodeUri(String uri) {
        try {
            return URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return URLEncoder.encode(uri);
        }
    }

    public static void main(String[] args) throws IOException {
        NanoHTTPD server = new NanoHTTPD(443, true, null, null);
        do {
            Thread.yield();
        } while (true);
    }

    static {
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.setProperty("https.protocols", "SSLv3,SSLv2Hello,TLSv1");
    }

    private class HTTPSession implements Runnable {
        private Socket mySocket;

        public HTTPSession(Socket s) {
            this.mySocket = s;
            Thread t = new Thread(this, "HTTP session handler");
            t.setDaemon(true);
            t.start();
        }

        private String readLine(DataInputStream in) throws IOException {
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
                InputStream is = this.mySocket.getInputStream();
                if (is == null) {
                    return;
                }
                DataInputStream in = new DataInputStream(is);
                String request = this.readLine(in);
                StringTokenizer st = new StringTokenizer(request == null ? "" : request);
                if (!st.hasMoreTokens()) {
                    NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (provided no input)");
                    throw new InterruptedException();
                }
                String method = st.nextToken();
                if (!st.hasMoreTokens()) {
                    NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (missing URI)");
                    throw new InterruptedException();
                }
                String uri = st.nextToken();
                Properties header = new Properties();
                Properties parms = new Properties();
                int qmi = uri.indexOf(63);
                if (qmi >= 0) {
                    header.put("QUERY_STRING", uri.substring(qmi + 1));
                    this.decodeParms(uri.substring(qmi + 1), parms);
                    uri = this.decodePercent(uri.substring(0, qmi));
                } else {
                    uri = this.decodePercent(uri);
                }
                header.put("REMOTE_ADDRESS", this.mySocket.getInetAddress().toString());
                if (st.hasMoreTokens()) {
                    String line = this.readLine(in);
                    while (line.trim().length() > 0) {
                        int p;
                        String key;
                        if (line.length() > 16384) {
                            NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (excess header length)");
                            this.sendError(NanoHTTPD.HTTP_TOOLARGE, "BAD REQUEST: header length is too large");
                        }
                        if ((p = line.indexOf(58)) == -1) {
                            NanoHTTPD.print_error("Dropped HTTP client from " + this.mySocket.getInetAddress().toString() + " (malformed header)");
                            this.sendError(NanoHTTPD.HTTP_BADREQUEST, "BAD REQUEST: malformed header");
                        }
                        if (line.substring(0, p).trim().toLowerCase().equals("content-length")) {
                            header.put("Content-Length", line.substring(p + 1).trim());
                        } else {
                            header.put(line.substring(0, p).trim(), line.substring(p + 1).trim());
                        }
                        line = this.readLine(in);
                    }
                }
                if (method.equalsIgnoreCase("POST")) {
                    long size = 0L;
                    String contentLength = header.getProperty("Content-Length");
                    if (contentLength != null) {
                        try {
                            size = Integer.parseInt(contentLength);
                        } catch (NumberFormatException ex) {
                            // empty catch block
                        }
                    }
                    if (size > 0xA00000L) {
                        this.sendError(NanoHTTPD.HTTP_TOOLARGE, "BAD REQUEST: Request Entity Too Large");
                    }
                    if (size > 0L) {
                        byte[] all = new byte[(int) size];
                        in.readFully(all, 0, (int) size);
                        if (NanoHTTPD.MIME_DEFAULT_BINARY.equals(header.getProperty("Content-Type")) || NanoHTTPD.this.alwaysRaw(uri)) {
                            ByteArrayInputStream bis = new ByteArrayInputStream(all);
                            parms.put("length", (long) all.length);
                            parms.put("input", bis);
                        } else {
                            this.decodeParms(new String(all), parms);
                        }
                    }
                }
                Response r = NanoHTTPD.this.serve(uri, method, header, parms);
                if (method.equalsIgnoreCase("HEAD")) {
                    r.data = null;
                }
                if (r == null) {
                    this.sendError(NanoHTTPD.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
                } else {
                    this.sendResponse(r.status, r.mimeType, r.header, r.data);
                }
                in.close();
            } catch (IOException ioe) {
                try {
                    this.sendError(NanoHTTPD.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (Throwable t) {
                }
            } catch (InterruptedException ie) {
                // empty catch block
            }
        }

        private String decodePercent(String str) throws InterruptedException {
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (Exception ex) {
                this.sendError(NanoHTTPD.HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
                return null;
            }
        }

        private void decodeParms(String parms, Properties p) throws InterruptedException {
            if (parms == null) {
                return;
            }
            StringTokenizer st = new StringTokenizer(parms, "&");
            while (st.hasMoreTokens()) {
                String e = st.nextToken();
                int sep = e.indexOf(61);
                if (sep < 0) continue;
                p.put(this.decodePercent(e.substring(0, sep)).trim(), this.decodePercent(e.substring(sep + 1)));
            }
        }

        private void sendError(String status, String msg) throws InterruptedException {
            this.sendResponse(status, NanoHTTPD.MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
            throw new InterruptedException();
        }

        private void sendResponse(String status, String mime, Map header, InputStream data) {
            try {
                if (status == null) {
                    throw new Error("sendResponse(): Status can't be null.");
                }
                OutputStream out = this.mySocket.getOutputStream();
                PrintWriter pw = new PrintWriter(out);
                pw.print("HTTP/1.1 " + status + " \r\n");
                if (header != null && header.get("Content-Type") != null) {
                    pw.print("Content-Type: " + header.get("Content-Type") + "\r\n");
                } else if (mime != null) {
                    pw.print("Content-Type: " + mime + "\r\n");
                }
                if (header == null || header.get("Date") == null) {
                    pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");
                }
                if (header != null) {
                    for (Object o : header.entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        if ("Content-Type".equals(key)) continue;
                        pw.print(key + ": " + value + "\r\n");
                    }
                }
                pw.print("\r\n");
                pw.flush();
                if (data != null) {
                    int read2;
                    byte[] buff = new byte[2048];
                    while ((read2 = data.read(buff, 0, 2048)) > 0) {
                        out.write(buff, 0, read2);
                    }
                }
                out.flush();
                out.close();
                if (data != null) {
                    data.close();
                }
            } catch (IOException ioe) {
                try {
                    this.mySocket.close();
                } catch (Throwable t) {
                    // empty catch block
                }
            }
        }
    }

    public static class TrustEverything implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] ax509certificate, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] ax509certificate, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}

