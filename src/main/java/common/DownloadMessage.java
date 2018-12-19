package common;

import java.io.Serializable;

public class DownloadMessage implements Serializable {
    public static final int DOWNLOAD_START = 0;
    public static final int DOWNLOAD_CHUNK = 1;
    public static final int DOWNLOAD_DONE = 2;
    public static final int DOWNLOAD_ERROR = 3;
    protected String id;
    protected long size = 0L;
    protected byte[] data = null;
    protected String message = null;
    protected int type;

    protected DownloadMessage(int type, String id) {
        this.type = type;
        this.id = id;
    }

    public static DownloadMessage Error(String id, String message) {
        DownloadMessage d = new DownloadMessage(3, id);
        d.message = message;
        return d;
    }

    public static DownloadMessage Chunk(String id, byte[] data) {
        DownloadMessage d = new DownloadMessage(1, id);
        d.data = data;
        return d;
    }

    public static DownloadMessage Done(String id) {
        return new DownloadMessage(2, id);
    }

    public static DownloadMessage Start(String id, long size) {
        DownloadMessage d = new DownloadMessage(0, id);
        d.size = size;
        return d;
    }

    public String getError() {
        if (this.type != 3) {
            throw new RuntimeException("Wrong message type for that info");
        }
        return this.message;
    }

    public byte[] getData() {
        if (this.type != 1) {
            throw new RuntimeException("Wrong message type for that info");
        }
        return this.data;
    }

    public long getSize() {
        if (this.type != 0) {
            throw new RuntimeException("Wrong message type for that info");
        }
        return this.size;
    }

    public int getType() {
        return this.type;
    }

    public String id() {
        return this.id;
    }
}

