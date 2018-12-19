package beacon.dns;

import common.CommonUtils;
import dns.DNSServer;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    protected Map checks = new HashMap();

    public boolean contains(String id, String nonce) {
        if (!CommonUtils.isNumber(id)) {
            return true;
        }
        Entry entry = (Entry) this.checks.get(id);
        if (entry == null) {
            return false;
        }
        return entry.items.containsKey(nonce);
    }

    public DNSServer.Response get(String id, String nonce) {
        if (!CommonUtils.isNumber(id)) {
            return DNSServer.A(0L);
        }
        Entry entry = (Entry) this.checks.get(id);
        return (DNSServer.Response) entry.items.get(nonce);
    }

    public void add(String id, String nonce, DNSServer.Response item) {
        Entry entry = (Entry) this.checks.get(id);
        if (entry == null) {
            entry = new Entry();
            this.checks.put(id, entry);
        }
        entry.items.put(nonce, item);
    }

    public void purge(String id) {
        Entry items = (Entry) this.checks.get(id);
        if (items == null) {
            return;
        }
        if (items.txcount >= 15L) {
            this.checks.remove(id);
        } else {
            ++items.txcount;
        }
    }

    private static class Entry {
        public Map items = new HashMap();
        public long txcount = 0L;

        private Entry() {
        }
    }

}

