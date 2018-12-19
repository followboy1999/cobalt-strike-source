package socks;

import common.MudgeSanity;

import java.io.IOException;
import java.net.Socket;

public class PortForwardClient
        extends BasicClient {
    protected String fhost;
    protected int fport;

    public PortForwardClient(SocksProxy parent, Socket client, int chid, String host, int port) {
        this.client = client;
        this.parent = parent;
        this.chid = chid;
        this.fhost = host;
        this.fport = port;
        new Thread(this, "SOCKS4a Proxy INIT").start();
    }

    @Override
    public void run() {
        try {
            this.setup();
            this.parent.fireEvent(ProxyEvent.EVENT_CONNECT(this.chid, this.fhost, this.fport));
        } catch (IOException ioex) {
            MudgeSanity.logException("port forward client", ioex, false);
        }
    }
}

