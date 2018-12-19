package graph;

import java.util.*;

public class TouchList
        extends LinkedList {
    protected Set touched = new HashSet();

    public void startUpdates() {
        this.touched.clear();
    }

    public void touch(Object key) {
        this.touched.add(key);
    }

    public List clearUntouched() {
        LinkedList results = new LinkedList();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Object j = i.next();
            if (this.touched.contains(j)) continue;
            results.add(j);
            i.remove();
        }
        return results;
    }
}

