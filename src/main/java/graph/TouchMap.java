package graph;

import java.util.*;

public class TouchMap
        extends HashMap {
    protected Set touched = new HashSet();

    public void startUpdates() {
        this.touched.clear();
    }

    public void touch(Object key) {
        this.touched.add(key);
    }

    public List clearUntouched() {
        LinkedList results = new LinkedList();
        Iterator i = this.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry j = (Map.Entry) i.next();
            if (this.touched.contains(j.getKey())) continue;
            results.add(j);
            i.remove();
        }
        return results;
    }
}

