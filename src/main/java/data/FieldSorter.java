package data;

import java.util.Comparator;
import java.util.Map;

public class FieldSorter implements Comparator {
    protected String field;
    protected Comparator smarts;

    public FieldSorter(String field, Comparator smarts) {
        this.field = field;
        this.smarts = smarts;
    }

    public int compare(Object o1, Object o2) {
        Map a1 = (Map) o1;
        Map a2 = (Map) o2;
        return this.smarts.compare(a1.get(this.field), a2.get(this.field));
    }
}

