package common;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

public class TeamSocket {
    protected String from;
    protected boolean connected = true;
    protected LinkedList<DisconnectListener> listeners = new LinkedList<>();
    protected Socket client;
    protected OutputStream bout = null;

    public TeamSocket(Socket client) throws Exception {
        this.client = client;
        client.setSoTimeout(0);
        this.from = client.getInetAddress().getHostAddress();
    }

    public void addDisconnectListener(DisconnectListener l) {
        synchronized (this) {
            this.listeners.add(l);
        }
    }

    public void fireDisconnectEvent() {
        synchronized (this) {
            for (DisconnectListener l : this.listeners) {
                l.disconnected(this);
            }
            this.listeners.clear();
        }
    }

    public boolean isConnected() {
        synchronized (this) {
            return this.connected;
        }
    }

    public Object readObject() {
        try {
            if (this.isConnected()) {
                ObjectInputStream in = new ObjectInputStream(this.client.getInputStream());
                return in.readUnshared();
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("client (" + this.from + ") read", ioex, true);
            this.close();
        } catch (ClassNotFoundException cnf) {
            MudgeSanity.logException("class not found", cnf, false);
            this.close();
        } catch (Exception ex) {
            MudgeSanity.logException("client (" + this.from + ") read", ex, false);
            this.close();
        }
        return null;
    }

    public void close() {
        if (!this.isConnected()) {
            return;
        }
        synchronized (this) {
            try {
                this.connected = false;
                if (this.bout != null) {
                    this.bout.close();
                }
                if (this.client != null) {
                    this.client.close();
                }
            } catch (Exception ex) {
                MudgeSanity.logException("client (" + this.from + ") close", ex, false);
            }
            this.fireDisconnectEvent();
        }
    }

    public void writeObject(Object data) {
        if (!this.isConnected()) {
            return;
        }
        try {
            synchronized (this.client) {
                if (this.bout == null) {
                    this.bout = new BufferedOutputStream(this.client.getOutputStream(), 262144);
                }
                ObjectOutputStream out = new ObjectOutputStream(this.bout);
                out.writeUnshared(data);
                out.flush();
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("client (" + this.from + ") write", ioex, true);
            this.close();
        } catch (Exception ex) {
            MudgeSanity.logException("client (" + this.from + ") write", ex, false);
            this.close();
        }
    }
}

