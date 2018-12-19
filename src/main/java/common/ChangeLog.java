package common;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class ChangeLog implements Serializable {
    public static final int CHANGE_ADD = 1;
    public static final int CHANGE_ADDNEW = 2;
    public static final int CHANGE_UPDATE = 3;
    public static final int CHANGE_DELETE = 4;
    protected LinkedList<ChangeEntry> changes = new LinkedList<>();
    protected long preid = 0L;
    protected long postid = 0L;
    protected String name;

    public int size() {
        return this.changes.size();
    }

    public boolean isDifferent() {
        return this.changes.size() > 0;
    }

    public ChangeLog(String name) {
        this.name = name;
    }

    public void add(String key, Map entry) {
        this.changes.add(new ChangeEntry(1, key, entry));
    }

    public void addnew(String key, Map entry) {
        this.changes.add(new ChangeEntry(2, key, entry));
    }

    public void update(String key, Map entry) {
        this.changes.add(new ChangeEntry(3, key, entry));
    }

    public void delete(String key) {
        this.changes.add(new ChangeEntry(4, key, null));
    }

    protected long pre(Map data) {
        long _preid = 0L;
        if (this.preid == 0L) {
            this.preid = _preid;
        } else if (_preid != this.preid) {
        }
        return _preid;
    }

    protected void post(Map data, long _preid) {
    }

    public void applyOptimize(Map<String, Map> data) {
        long _preid = this.pre(data);
        Iterator<ChangeEntry> i = this.changes.iterator();
        for (ChangeEntry change : this.changes) {
            this.actOptimize(change, data);
            if (change.isNeccessary()) continue;
            this.changes.remove(change);
        }
        this.post(data, _preid);
    }

    public void applyForce(Map<String, Map> data) {
        long _preid = this.pre(data);
        for (ChangeEntry change : this.changes) {
            this.actForce(change, data);
        }
        this.post(data, _preid);
    }

    protected boolean same(Map a, Map b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (Object o : a.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            Object fromb = b.get(next.getKey());

            if ((fromb != null) || (next.getValue() != null)) {
                if (fromb == null) {
                    return false;
                }
                if (next.getValue() == null) {
                    return false;
                }
                if (!fromb.toString().equals(next.getValue().toString())) {
                    return false;
                }
            }
        }

        return true;
    }


    protected void actForce(ChangeEntry change, Map<String, Map> data) {
        switch (change.type()) {
            case CHANGE_ADD: {
                data.put(change.key(), change.entry());
                break;
            }
            case CHANGE_ADDNEW: {
                if (data.containsKey(change.key())) break;
                data.put(change.key(), change.entry());
                break;
            }
            case CHANGE_UPDATE: {
                if (!data.containsKey(change.key())) {
                    data.put(change.key(), change.entry());
                    break;
                }
                Map current = data.get(change.key());
                for (Object o : change.entry().entrySet()) {
                    Map.Entry next = (Map.Entry) o;
                    current.put(next.getKey(), next.getValue());
                }
                break;
            }
            case CHANGE_DELETE: {
                data.remove(change.key());
            }
        }
    }

    protected void actOptimize(ChangeEntry change, Map<String, Map> data) {
        switch (change.type()) {
            case CHANGE_ADD: {
                if (data.containsKey(change.key())) {
                    Map old = data.get(change.key());
                    if (!this.same(old, change.entry())) {
                        data.put(change.key(), change.entry());
                        break;
                    }
                    change.kill();
                    break;
                }
                data.put(change.key(), change.entry());
                break;
            }
            case CHANGE_ADDNEW: {
                if (!data.containsKey(change.key())) {
                    data.put(change.key(), change.entry());
                    break;
                }
                change.kill();
                break;
            }
            case CHANGE_UPDATE:
                if (!data.containsKey(change.key())) {
                    data.put(change.key(), change.entry());
                } else {
                    Map current = data.get(change.key());
                    boolean useful = false;
                    for (Object o : change.entry().entrySet()) {
                        Map.Entry next = (Map.Entry) o;
                        Object _check = current.get(next.getKey());
                        if ((_check != null) || (next.getValue() != null)) {
                            if (_check == null) {
                                current.put(next.getKey(), next.getValue());
                                useful = true;
                            } else if (next.getValue() != null) {
                                if (!next.getValue().toString().equals(_check.toString())) {
                                    current.put(next.getKey(), next.getValue());
                                    useful = true;
                                }
                            } else {
                                current.put(next.getKey(), next.getValue());
                                useful = true;
                            }
                        }
                    }
                    if (!useful)
                        change.kill();
                }
                break;
            case CHANGE_DELETE: {
                if (data.containsKey(change.key())) {
                    data.remove(change.key());
                    break;
                }
                change.kill();
            }
        }
    }

    public void print() {
        CommonUtils.print_info("Change Log...");
        for (ChangeEntry change : this.changes) {
            change.print();
        }
    }

    public class ChangeEntry implements Serializable {
        protected int type;
        protected String key;
        protected Map entry;
        protected boolean needed = true;

        public ChangeEntry(int type, String key, Map entry) {
            this.type = type;
            this.key = key;
            this.entry = entry;
        }

        public void kill() {
            this.needed = false;
        }

        public boolean isNeccessary() {
            return this.needed;
        }

        public String key() {
            return this.key;
        }

        public int type() {
            return this.type;
        }

        public Map entry() {
            return this.entry;
        }

        public void print() {
            switch (this.type) {
                case CHANGE_ADD: {
                    CommonUtils.print_info("\tAdd:\n\t\t" + this.key + "\n\t\t" + this.entry);
                    break;
                }
                case CHANGE_ADDNEW: {
                    CommonUtils.print_info("\tAddNew:\n\t\t" + this.key + "\n\t\t" + this.entry);
                    break;
                }
                case CHANGE_UPDATE: {
                    CommonUtils.print_info("\tUpdate:\n\t\t" + this.key + "\n\t\t" + this.entry);
                    break;
                }
                case CHANGE_DELETE: {
                    CommonUtils.print_info("\tDelete:\n\t\t" + this.key);
                }
            }
        }
    }

}

