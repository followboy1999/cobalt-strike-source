package socks;

import common.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SocksProxy {
    protected int id = 0;
    protected List clients = new LinkedList();
    protected List listeners = new LinkedList();
    protected LinkedList reads = new LinkedList();
    protected int readq = 0;
    protected String error = "";
    public static final int SOCKS_MAX_CLIENTS = 67108864;

    public boolean hasSpace() {
        LinkedList linkedList = this.reads;
        synchronized (linkedList) {
            return this.readq < 1048576;
        }
    }

    public void read(byte[] data) {
        SocksData doofus = new SocksData();
        doofus.data = data;
        LinkedList linkedList = this.reads;
        synchronized (linkedList) {
            this.reads.add(doofus);
            this.readq += data.length;
        }
    }

    public byte[] grab(int length) {
        if (length <= 0) {
            return new byte[0];
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream(length);
        int total = 0;
        LinkedList linkedList = this.reads;
        synchronized (linkedList) {
            SocksData next;
            while (total < length && (next = (SocksData) this.reads.peek()) != null && total + next.data.length < length) {
                this.reads.removeFirst();
                result.write(next.data, 0, next.data.length);
                total += next.data.length;
                this.readq -= next.data.length;
            }
        }
        return result.toByteArray();
    }

    public void fireEvent(ProxyEvent event) {
        for (Object listener : this.listeners) {
            ProxyListener l = (ProxyListener) listener;
            l.proxyEvent(this, event);
        }
    }

    public void addClient(BasicClient aclient) {
        synchronized (this) {
            Iterator i = this.clients.iterator();
            while (i.hasNext()) {
                BasicClient client = (BasicClient) i.next();
                if (client.isAlive()) continue;
                i.remove();
            }
            this.clients.add(aclient);
        }
    }

    public void killClients() {
        synchronized (this) {
            for (Object client1 : this.clients) {
                BasicClient client = (BasicClient) client1;
                if (!client.isAlive()) continue;
                client.die();
            }
            this.clients.clear();
        }
    }

    private BasicClient findClient(int chid, String desc) {
        synchronized (this) {
            for (Object client1 : this.clients) {
                BasicClient client = (BasicClient) client1;
                if (!client.isAlive() || client.chid != chid) continue;
                return client;
            }
        }
        CommonUtils.print_warn("-- Could not find chid " + chid + " for " + desc + " (closing)");
        Thread.currentThread();
        Thread.dumpStack();
        this.fireEvent(ProxyEvent.EVENT_CLOSE(chid));
        return null;
    }

    public void addProxyListener(ProxyListener l) {
        this.listeners.add(l);
    }

    public void resume(int chid) {
        BasicClient client = this.findClient(chid, "resume");
        if (client != null) {
            client.start();
        }
    }

    public void write(int chid, byte[] data, int offset, int length) {
        BasicClient client = this.findClient(chid, "write");
        if (client != null) {
            client.write(data, offset, length);
        }
    }

    public void die(int chid) {
        BasicClient client = this.findClient(chid, "die");
        if (client != null) {
            client.die();
        }
    }

    public int nextId() {
        int temp_id;
        synchronized (this) {
            temp_id = this.id;
            this.id = (this.id + 1) % 67108864;
        }
        return temp_id;
    }

    private static final class SocksData {
        public byte[] data;

        private SocksData() {
        }
    }

}

