package socks;

public class ProxyEvent {
    public static final int PROXY_CLOSE = 0;
    public static final int PROXY_CONNECT = 1;
    public static final int PROXY_LISTEN = 2;
    public static final int PROXY_READ = 3;
    public int chid;
    public int type;
    public byte[] data;
    public int length;
    public String host;
    public int port;

    public static ProxyEvent EVENT_CLOSE(int chid) {
        return new ProxyEvent(0, chid);
    }

    public static ProxyEvent EVENT_CONNECT(int chid, String host, int port) {
        return new ProxyEvent(1, chid, host, port);
    }

    public static ProxyEvent EVENT_LISTEN(int chid, String host, int port) {
        return new ProxyEvent(2, chid, host, port);
    }

    public static ProxyEvent EVENT_READ(int chid, byte[] data, int length) {
        return new ProxyEvent(3, chid, data, length);
    }

    public ProxyEvent(int type, int chid) {
        this.type = type;
        this.chid = chid;
    }

    public ProxyEvent(int type, int chid, byte[] data, int length) {
        this.chid = chid;
        this.type = type;
        this.data = data;
        this.length = length;
    }

    public ProxyEvent(int type, int chid, String host, int port) {
        this.chid = chid;
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public int getType() {
        return this.type;
    }

    public int getChannelId() {
        return this.chid;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getDataLength() {
        return this.length;
    }

    public String toString() {
        switch (this.type) {
            case 0: {
                return "close@" + this.chid;
            }
            case 1: {
                return "connect to " + this.host + ":" + this.port + "@" + this.chid;
            }
            case 2: {
                return "listen on " + this.host + ":" + this.port + "@" + this.chid;
            }
            case 3: {
                return "read " + this.length + " bytes@" + this.chid;
            }
        }
        return "uknown event type: " + this.type + "@" + this.chid;
    }
}

