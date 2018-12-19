package socks;

import java.io.IOException;
import java.net.Socket;

public class ReversePortForwardClient
        extends BasicClient {
    protected String fhost;
    protected int fport;
    protected int lport;

    public ReversePortForwardClient(SocksProxy parent, int chid, int lport, String rhost, int rport) {
        this.client = null;
        this.parent = parent;
        this.chid = chid;
        this.fhost = rhost;
        this.fport = rport;
        this.lport = lport;
    }

    @Override
    public void start() {
        try {
            this.client = new Socket(this.fhost, this.fport);
            this.setup();
            super.start();
        } catch (IOException ioex) {
            this.die();
        }
    }

    @Override
    public void run() {
    }
}

