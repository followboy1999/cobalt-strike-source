package common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RangeList {
    protected List results;
    protected String targets;
    protected boolean hasError = false;
    protected String description = "";
    public static final int ENTRY_BARE = 1;
    public static final int ENTRY_RANGE = 2;

    public boolean hasError() {
        return this.hasError;
    }

    public String getError() {
        return this.description;
    }

    public Entry Bare(String value) {
        Entry temp = new Entry();
        temp.type = 1;
        temp.value = CommonUtils.toNumber(value, 0);
        return temp;
    }

    public Entry Range(long start, long end) {
        Entry temp = new Entry();
        temp.type = 2;
        temp.start = start;
        temp.end = end;
        return temp;
    }

    public LinkedList parse() {
        LinkedList<Entry> results = new LinkedList<>();
        String[] records = this.targets.split(",");
        for (int x = 0; x < records.length; ++x) {
            long first;
            String[] temp;
            long next;
            records[x] = records[x].trim();
            if (records[x].matches("\\d+-\\d+")) {
                temp = records[x].split("-");
                first = CommonUtils.toNumber(temp[0], 0);
                next = CommonUtils.toNumber(temp[1], 0);
                results.add(this.Range(first, next));
                continue;
            }
            if (records[x].matches("\\d++\\d+")) {
                temp = records[x].split("+");
                first = CommonUtils.toNumber(temp[0], 0);
                next = CommonUtils.toNumber(temp[1], 0);
                results.add(this.Range(first, first + next));
                continue;
            }
            results.add(this.Bare(records[x]));
        }
        return results;
    }

    public RangeList(String targets) {
        this.targets = targets;
        this.results = this.parse();
    }

    public Iterator iterator() {
        return this.results.iterator();
    }

    public List toList() {
        LinkedList<Long> results = new LinkedList<>();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (e.type == 1) {
                results.add(e.value);
                continue;
            }
            if (e.type != 2) continue;
            for (long x = e.start; x < e.end; ++x) {
                results.add(x);
            }
        }
        return results;
    }

    public int random() {
        LinkedList<Integer> candidates = new LinkedList<>();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (e.type == 1) {
                candidates.add((int) e.value);
                continue;
            }
            if (e.type != 2) continue;
            candidates.add((int) e.start + CommonUtils.rand((int) e.end - (int) e.start));
        }
        return (Integer) CommonUtils.pick(candidates);
    }

    public boolean hit(long target) {
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (!(e.type == 1 ? e.value == target : e.type == 2 && target >= e.start && target < e.end)) continue;
            return true;
        }
        return false;
    }

    private static class Entry {
        public int type;
        public long value;
        public long start;
        public long end;

        private Entry() {
        }
    }

}

