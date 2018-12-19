package filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataFilter {
    protected LinkedList<Entry> criteria = null;

    public void reset() {
        this.criteria = null;
    }

    protected void addCriteria(String column, Criteria crit, boolean negate) {
        if (this.criteria == null) {
            this.criteria = new LinkedList<>();
        }
        Entry temp = new Entry();
        temp.crit = negate ? new NegateCriteria(crit) : crit;
        temp.col = column;
        this.criteria.add(temp);
    }

    public void checkWildcard(String column, String value) {
        this.addCriteria(column, new WildcardCriteria(value), false);
    }

    public void checkWildcard(String column, String value, boolean negate) {
        this.addCriteria(column, new WildcardCriteria(value), negate);
    }

    public void checkLiteral(String column, String value) {
        this.addCriteria(column, new LiteralCriteria(value), false);
    }

    public void checkNTLMHash(String column, boolean negate) {
        this.addCriteria(column, new NTLMHashCriteria(), negate);
    }

    public void checkNetwork(String column, String desc, boolean negate) {
        this.addCriteria(column, new NetworkCriteria(desc), negate);
    }

    public void checkNumber(String column, String desc, boolean negate) {
        this.addCriteria(column, new RangeCriteria(desc), negate);
    }

    public void checkBeacon(String column, boolean negate) {
        this.addCriteria(column, new BeaconCriteria(), negate);
    }

    public List apply(List<Map<String, Object>> items) {
        if (this.criteria == null) {
            return items;
        }
        List<Map<String, Object>> result = new LinkedList<>(items);

        for (Map<String, Object> i : result) {
            if (this.test(i)) continue;
            result.remove(i);
        }
        return result;
    }

    public boolean test(Map subject) {
        if (this.criteria == null) {
            return true;
        }
        for (Entry current : this.criteria) {
            Object entry = subject.get(current.col);
            if (current.crit.test(entry)) continue;
            return false;
        }
        return true;
    }

    public String toString() {
        if (this.criteria.size() == 1) {
            return "1 filter";
        }
        return this.criteria.size() + " filters";
    }

    private static class Entry {
        public Criteria crit;
        public String col;

        private Entry() {
        }
    }

}

