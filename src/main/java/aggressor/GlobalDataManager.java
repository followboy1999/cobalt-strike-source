package aggressor;

import common.Callback;
import common.CommonUtils;
import common.MudgeSanity;

import java.util.*;

public class GlobalDataManager implements Runnable,
        GenericDataManager {
    protected HashMap<Object, HashMap> store = new HashMap<>();
    protected HashMap subs = new HashMap<>();
    protected LinkedList reports = new LinkedList();
    protected HashSet<String> globals = new HashSet<>();
    protected static GlobalDataManager manager = new GlobalDataManager();

    public static GlobalDataManager getGlobalDataManager() {
        return manager;
    }

    @Override
    public WindowCleanup unsubOnClose(String key, Callback l) {
        return new WindowCleanup(this, key, l);
    }

    public boolean isGlobal(String key) {
        return this.globals.contains(key);
    }

    public GlobalDataManager() {
        this.globals.add("listeners");
        this.globals.add("sites");
        this.globals.add("tokens");
        new Thread(this, "Global Data Manager").start();
    }

    @Override
    public void unsub(String key, Callback listener) {

        synchronized (this) {
            List listeners = (List) this.subs.get(key);
            listeners.remove(listener);
        }
    }

    protected List getSubs(String key) {

        synchronized (this) {
            LinkedList listeners = (LinkedList) this.subs.get(key);
            if (listeners == null) {
                listeners = new LinkedList();
                this.subs.put(key, listeners);
            }
            return listeners;
        }
    }

    protected List getSubsSafe(String key) {

        synchronized (this) {
            return new LinkedList(this.getSubs(key));
        }
    }

    @Override
    public void subscribe(String key, Callback l) {

        synchronized (this) {
            this.getSubs(key).add(l);
        }
    }

    public void cleanup() {

        synchronized (this) {
            Iterator i = this.store.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry next = (Map.Entry) i.next();
                DataManager dmgr = (DataManager) next.getKey();
                if (dmgr.isAlive()) continue;
                i.remove();
                CommonUtils.print_stat("Released data manager: " + dmgr);
            }
        }
    }

    public void wait(DataManager dmgr) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000L) {

            synchronized (this) {
                if (this.store.containsKey(dmgr)) {
                    break;
                }
            }
            Thread.yield();
        }
    }

    public void put(DataManager dmgr, String key, Object data) {

        synchronized (this) {
            if (!this.store.containsKey(dmgr)) {
                this.store.put(dmgr, new HashMap<>());
            }
            HashMap mystore = this.store.get(dmgr);
            mystore.put(key, data);
        }
    }

    protected Map getMap(String key) {
        HashMap result = new HashMap();

        synchronized (this) {
            this.cleanup();
            for (HashMap mystore : this.store.values()) {
                HashMap lresult = (HashMap)  mystore.get(key);
                result.putAll(lresult);
            }
        }
        return result;
    }

    protected List getList(String key) {
        LinkedList result = new LinkedList();

        synchronized (this) {
            this.cleanup();
            for (HashMap mystore : this.store.values()) {
                List lresult = (List) mystore.get(key);
                if (lresult == null) continue;
                result.addAll(lresult);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getMapSafe(String key) {
        return (Map) this.get(key, Collections.emptyMap());
    }

    @Override
    public LinkedList getListSafe(String key) {
        return (LinkedList) this.get(key, new LinkedList<>());
    }

    @Override
    public Object get(String key, Object defaultz) {
        if (key.equals("listeners")) {
            return this.getMap("listeners");
        }
        if (key.equals("sites")) {
            return this.getList("sites");
        }
        if (key.equals("tokens")) {
            return this.getList("tokens");
        }
        CommonUtils.print_error("Value: " + key + " is not a global data value! [BUG!!]");
        return defaultz;
    }

    public void process(DataManager dmgr, String key, Object data) {
        Object result;

        synchronized (this) {
            this.put(dmgr, key, data);
            result = this.get(key, data);
        }
        for (Object o : this.getSubsSafe(key)) {
            Callback next = (Callback) o;
            next.result(key, result);
        }
    }

    public void report(DataManager dmgr, String key, Object data) {

        synchronized (this) {
            this.reports.add(new TripleZ(dmgr, key, data));
        }
    }

    protected TripleZ grab() {

        synchronized (this) {
            return (TripleZ) this.reports.pollFirst();
        }
    }

    @Override
    public void run() {
        long clean = System.currentTimeMillis() + 10000L;
        TripleZ next = null;
        try {
            do {
                if (System.currentTimeMillis() > clean) {
                    clean = System.currentTimeMillis() + 10000L;
                    this.cleanup();
                }
                if ((next = this.grab()) == null) {
                    Thread.sleep(1000L);
                    continue;
                }
                this.process(next.dmgr, next.key, next.data);
                Thread.yield();
            } while (true);
        } catch (Exception ex) {
            MudgeSanity.logException("GDM Loop: " + next, ex, false);
            return;
        }
    }

    private static class TripleZ {
        public DataManager dmgr;
        public String key;
        public Object data;

        public TripleZ(DataManager dmgr, String key, Object data) {
            this.dmgr = dmgr;
            this.key = key;
            this.data = data;
        }

        public String toString() {
            return this.dmgr + "; " + this.key + " => " + this.data;
        }
    }

}

