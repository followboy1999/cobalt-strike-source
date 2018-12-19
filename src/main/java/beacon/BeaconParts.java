package beacon;

import common.CommonUtils;
import common.Packer;

import java.util.HashMap;
import java.util.Map;

public class BeaconParts {
    protected Map parts = new HashMap();

    public void start(String id, int length) {
        Part part = new Part();
        part.length = length;
        if (length <= 0) {
            return;
        }
        synchronized (this) {
            this.parts.put(id, part);
        }
    }

    public boolean isReady(String id) {
        Part temp;
        synchronized (this) {
            temp = (Part) this.parts.get(id);
        }
        return temp != null && temp.buffer.size() >= (long) temp.length;
    }

    public boolean hasPart(String id) {
        Part temp;
        synchronized (this) {
            temp = (Part) this.parts.get(id);
        }
        return temp != null;
    }

    public void put(String id, byte[] data) {
        Part temp;
        synchronized (this) {
            temp = (Part) this.parts.get(id);
        }
        if (temp == null) {
            CommonUtils.print_error("CALLBACK_CHUNK_SEND " + id + ": no pending transmission");
            return;
        }
        temp.buffer.addString(data, data.length);
    }

    public byte[] data(String id) {
        Part temp;
        synchronized (this) {
            temp = (Part) this.parts.get(id);
            this.parts.remove(id);
        }
        if (temp == null) {
            return new byte[0];
        }
        return temp.buffer.getBytes();
    }

    public static class Part {
        public int length;
        public Packer buffer = new Packer();
    }

}

