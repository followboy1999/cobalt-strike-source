package cloudstrike;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Response {
    public String status;
    public String mimeType;
    public InputStream data;
    public LinkedHashMap<String,String> header = new LinkedHashMap<>();
    public LinkedHashMap<String,String> params = new LinkedHashMap<>();
    public String uri = "";
    public long size = 0L;
    public long offset = 0L;

    public Response() {
        this.status = "200 OK";
    }

    public Response(String status, String mimeType, InputStream data) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
    }

    public Response(String status, String mimeType, InputStream data, long size) {
        this(status, mimeType, data);
        this.size = size;
        this.addHeader("Content-Length", size + "");
    }

    public Response(String status, String mimeType, String txt) {
        byte[] r = Response.toBytes(txt);
        this.status = status;
        this.mimeType = mimeType;
        this.data = new ByteArrayInputStream(r);
        this.size = r.length;
        this.addHeader("Content-Length", this.size + "");
    }

    public static byte[] toBytes(String data) {
        int length = data.length();
        byte[] r = new byte[length];
        for (int x = 0; x < length; ++x) {
            r[x] = (byte) data.charAt(x);
        }
        return r;
    }

    public void addHeader(String entry) {
        String[] data = entry.split(": ");
        if (data.length == 1) {
            this.addHeader(data[0], "");
        } else {
            this.addHeader(data[0], data[1]);
        }
    }

    public void addHeader(String name, String value) {
        if ("".equals(value)) {
            this.header.remove(name);
        } else {
            this.header.put(name, value);
        }
    }

    public void addParameter(String entry) {
        String[] data = entry.split("=");
        if (data.length == 1) {
            this.params.put(data[0], "");
        } else {
            this.params.put(data[0], data[1]);
        }
    }
}

