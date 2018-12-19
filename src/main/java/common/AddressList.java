package common;

import graph.Route;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AddressList {
    protected List results;
    protected String targets;
    protected boolean hasError;
    protected String description;
    public static final int ENTRY_BARE = 1;
    public static final int ENTRY_RANGE = 2;
    private static final String IPADDR = "\\d+\\.\\d+\\.\\d+\\.\\d+";

    public boolean hasError() {
        return this.hasError;
    }

    public String getError() {
        return this.description;
    }

    public Entry Bare(String value) {
        Entry temp = new Entry();
        temp.type = 1;
        temp.address = value;
        return temp;
    }

    public Entry Range(long start, long end) {
        Entry temp = new Entry();
        temp.type = 2;
        temp.start = start;
        temp.end = end;
        return temp;
    }

    public String check(String addr) {
        String[] temp = addr.split("\\.");
        int a = CommonUtils.toNumber(temp[0], -1);
        int b = CommonUtils.toNumber(temp[1], -1);
        int c = CommonUtils.toNumber(temp[2], -1);
        int d = CommonUtils.toNumber(temp[3], -1);
        if (a >= 0 && b >= 0 && c >= 0 && d >= 0 && a < 256 && b < 256 && c < 256 && d < 256) {
            return addr;
        }
        this.hasError = true;
        this.description = addr + " is not an IPv4 address";
        return addr;
    }

    public LinkedList parse() {
        LinkedList<Entry> results = new LinkedList<>();
        String[] records = this.targets.split(",");
        for (int x = 0; x < records.length; ++x) {
            String addr;
            long start;
            String[] temp;
            records[x] = CommonUtils.trim(records[x]);
            if (records[x].matches("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+")) {
                temp = records[x].split("/");
                addr = this.check(temp[0]);
                int cidr = CommonUtils.toNumber(temp[1], 0);
                if (cidr < 0 || cidr > 32) {
                    this.hasError = true;
                    this.description = records[x] + " has invalid CIDR notation " + cidr;
                    continue;
                }
                long start2 = Route.ipToLong(addr);
                long end = start2 + (long) Math.pow(2.0, 32 - cidr);
                results.add(this.Range(start2, end));
                continue;
            }
            if (records[x].matches("\\d+\\.\\d+\\.\\d+\\.\\d+-\\d+")) {
                temp = records[x].split("-");
                addr = this.check(temp[0]);
                long next = CommonUtils.toNumber(temp[1], 0);
                start = Route.ipToLong(addr);
                if ((next -= start & 255L) <= 0L) {
                    this.hasError = true;
                    this.description = "Invalid range: " + next + " is less than " + (start & 255L);
                    continue;
                }
                results.add(this.Range(start, start + next));
                continue;
            }
            if (records[x].matches("\\d+\\.\\d+\\.\\d+\\.\\d++\\d+")) {
                temp = records[x].split("+");
                addr = this.check(temp[0]);
                long next = CommonUtils.toNumber(temp[1], 0);
                start = Route.ipToLong(addr);
                results.add(this.Range(start, start + next));
                continue;
            }
            if (records[x].matches("\\d+\\.\\d+\\.\\d+\\.\\d+-\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                long b;
                temp = records[x].split("-");
                String addrA = this.check(temp[0]);
                String addrB = this.check(temp[1]);
                long a = Route.ipToLong(addrA);
                if (a >= (b = Route.ipToLong(addrB))) {
                    this.hasError = true;
                    this.description = "Invalid range: " + addrA + " is greater than " + addrB;
                    continue;
                }
                results.add(this.Range(a, b));
                continue;
            }
            results.add(this.Bare(records[x]));
        }
        return results;
    }

    public AddressList(String targets) {
        this.targets = targets;
        this.results = this.parse();
        if (this.export().length > 2000) {
            this.hasError = true;
            this.description = "target list is too long";
        }
    }

    public Iterator iterator() {
        return this.results.iterator();
    }

    public static String toIP(long x) {
        long a = (x & -16777216L) >> 24;
        long b = (x & 0xFF0000L) >> 16;
        long c = (x & 65280L) >> 8;
        long d = x & 255L;
        return a + "." + b + "." + c + "." + d;
    }

    public List toList() {
        LinkedList<String> results = new LinkedList<>();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (e.type == 1) {
                results.add(e.address);
                continue;
            }
            if (e.type != 2) continue;
            for (long x = e.start; x < e.end; ++x) {
                results.add(AddressList.toIP(x));
            }
        }
        return results;
    }

    public boolean hit(String addr) {
        long target = Route.ipToLong(addr);
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (!(e.type == 1 ? addr.equals(e.address) : e.type == 2 && target >= e.start && target < e.end)) continue;
            return true;
        }
        return false;
    }

    public byte[] export() {
        Packer temp = new Packer();
        temp.little();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            temp.addInt(e.type);
            if (e.type == 1) {
                temp.addInt(e.address.length());
                temp.addString(e.address);
                continue;
            }
            if (e.type != 2) continue;
            temp.addInt(8);
            temp.addInt((int) e.start);
            temp.addInt((int) e.end);
        }
        return temp.getBytes();
    }

    private static class Entry {
        public int type;
        public String address;
        public long start;
        public long end;

        private Entry() {
        }
    }

}

