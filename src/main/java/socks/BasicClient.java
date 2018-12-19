package socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public abstract class BasicClient implements Runnable {
    protected Socket client = null;
    protected boolean alive = true;
    protected SocksProxy parent = null;
    protected int chid = 0;
    protected boolean started = false;
    protected InputStream in = null;
    protected OutputStream out = null;

    public void die() {
        synchronized (this) {
            this.alive = false;
        }
        this.parent.fireEvent(ProxyEvent.EVENT_CLOSE(this.chid));
        if (!this.started) {
            this.deny();
            return;
        }
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException ioex) {
            // empty catch block
        }
    }

    public boolean isAlive() {
        synchronized (this) {
            return this.alive;
        }
    }

    public BasicClient() {
    }

    public BasicClient(SocksProxy parent, Socket client, int chid) {
        this.client = client;
        this.parent = parent;
        this.chid = chid;
        new Thread(this, "SOCKS4a Proxy INIT").start();
    }

    protected void startReading(Socket client) {
        try {
            byte[] data = new byte[65536];
            int read2;
            while (this.isAlive()) {
                if (this.parent.hasSpace()) {
                    read2 = this.in.read(data);
                    if (read2 == -1) break;
                    this.parent.fireEvent(ProxyEvent.EVENT_READ(this.chid, data, read2));
                    continue;
                }
                Thread.sleep(250L);
            }
            this.die();
        } catch (InterruptedException iex) {
            this.die();
        } catch (IOException ioex) {
            this.die();
        }
    }

    public void setup() throws IOException {
        this.in = this.client.getInputStream();
        this.out = this.client.getOutputStream();
    }

    public void start() {
        new Thread(() -> BasicClient.this.startReading(BasicClient.this.client), "SOCKS4a Client Reader").start();
    }

    public void write(byte[] data, int start, int length) {
        try {
            this.out.write(data, start, length);
            this.out.flush();
        } catch (IOException ioex) {
            this.die();
        }
    }

    protected void deny() {
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (IOException ioex) {
            // empty catch block
        }
    }

    @Override
    public abstract void run();

}

