package beacon;

import java.util.*;

public class BeaconPipes {
    protected Map pipes = new HashMap();

    public void reset() {
        synchronized (this) {
            this.pipes = new HashMap();
        }
    }

    public void register(String parent, String child) {
        synchronized (this) {
            LinkedHashSet<String> next = (LinkedHashSet<String>) this.pipes.get(parent);
            if (next == null) {
                next = new LinkedHashSet<>();
                this.pipes.put(parent, next);
            }
            next.add(child);
        }
    }

    public void clear(String parent) {
        synchronized (this) {
            this.pipes.remove(parent);
        }
    }

    public List children(String parent) {
        synchronized (this) {
            LinkedHashSet next = (LinkedHashSet) this.pipes.get(parent);
            if (next == null) {
                return new LinkedList();
            }
            return new LinkedList(next);
        }
    }

    public void deregister(String parent, String child) {
        synchronized (this) {
            LinkedHashSet next = (LinkedHashSet) this.pipes.get(parent);
            if (next == null) {
                return;
            }
            Iterator i = next.iterator();
            while (i.hasNext()) {
                String temp = i.next() + "";
                if (!temp.equals(child)) continue;
                i.remove();
            }
        }
    }

    public boolean isChild(String parent, String child) {
        synchronized (this) {
            LinkedHashSet next = (LinkedHashSet) this.pipes.get(parent);
            if (next == null) {
                return false;
            }
            return next.contains(child);
        }
    }
}

