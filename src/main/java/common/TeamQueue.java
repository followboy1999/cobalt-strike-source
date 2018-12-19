package common;

import java.util.HashMap;
import java.util.LinkedList;

public class TeamQueue {
    protected TeamSocket socket;
    protected final HashMap<Long, Callback> callbacks = new HashMap<>();
    protected long reqno = 0L;
    protected TeamReader reader;
    protected TeamWriter writer;
    protected Callback subscriber = null;

    public TeamQueue(TeamSocket socket) {
        this.socket = socket;
        this.reader = new TeamReader();
        this.writer = new TeamWriter();
        new Thread(this.writer, "TeamQueue Writer").start();
        new Thread(this.reader, "TeamQueue Reader").start();
    }

    public void call(String name, Callback c) {
        this.call(name, null, c);
    }

    public void call(String name) {
        this.call(name, null, null);
    }

    public void call(String name, Object[] args) {
        this.call(name, args, null);
    }

    public void call(String name, Object[] args, Callback c) {
        if (c == null) {
            Request r = new Request(name, args, 0L);
            this.writer.addRequest(r);
        } else {
            synchronized (this.callbacks) {
                ++this.reqno;
                this.callbacks.put(this.reqno, c);
                Request r2 = new Request(name, args, this.reqno);
                this.writer.addRequest(r2);
            }
        }
    }

    public boolean isConnected() {
        return this.socket.isConnected();
    }

    public void close() {
        this.socket.close();
    }

    public void addDisconnectListener(DisconnectListener l) {
        this.socket.addDisconnectListener(l);
    }

    public void setSubscriber(Callback c) {
        synchronized (this) {
            this.subscriber = c;
        }
    }

    protected void processRead(Reply r) {
        Callback c;
        if (r.hasCallback()) {
            //noinspection SynchronizeOnNonFinalField
            synchronized (this.callbacks) {
                c = this.callbacks.get(r.getCallbackReference());
                this.callbacks.remove(r.getCallbackReference());
            }
            if (c != null) {
                c.result(r.getCall(), r.getContent());
            }
        } else {
            synchronized (this) {
                if (this.subscriber != null) {
                    this.subscriber.result(r.getCall(), r.getContent());
                }
            }
        }
    }

    private class TeamWriter implements Runnable {
        protected LinkedList<Request> requests = new LinkedList<>();

        protected Request grabRequest() {
            synchronized (this) {
                return this.requests.pollFirst();
            }
        }

        protected void addRequest(Request r) {
            synchronized (this) {
                if (r.size() > 100000) {
                    this.requests.removeFirst();
                }
                this.requests.add(r);
            }
        }

        @Override
        public void run() {
            while (TeamQueue.this.socket.isConnected()) {
                Request next = this.grabRequest();
                if (next != null) {
                    TeamQueue.this.socket.writeObject(next);
                    Thread.yield();
                    continue;
                }
                try {
                    Thread.sleep(25L);
                } catch (InterruptedException iex) {
                    MudgeSanity.logException("teamwriter sleep", iex, false);
                }
            }
        }
    }

    private class TeamReader implements Runnable {
        @Override
        public void run() {
            try {
                while (TeamQueue.this.socket.isConnected()) {
                    Reply r = (Reply) TeamQueue.this.socket.readObject();
                    TeamQueue.this.processRead(r);
                    Thread.yield();
                }
            } catch (Exception ex) {
                MudgeSanity.logException("team reader", ex, false);
                TeamQueue.this.close();
            }
        }
    }

}

