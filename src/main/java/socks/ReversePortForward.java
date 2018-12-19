package socks;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class ReversePortForward implements Mortal {
    protected ServerSocket server = null;
    protected SocksProxy broker;
    protected String fhost;
    protected int fport;
    protected int port;

    @Override
    public void die() {
    }

    @Override
    public Map toMap() {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("type", "reverse port forward");
        temp.put("port", this.port + "");
        temp.put("fhost", this.fhost);
        temp.put("fport", this.fport + "");
        return temp;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public ReversePortForward(SocksProxy broker, int port, String fhost, int fport) {
        this.broker = broker;
        this.port = port;
        this.fhost = fhost;
        this.fport = fport;
    }

    public void accept(int sid) {
        ReversePortForwardClient client = new ReversePortForwardClient(this.broker, sid, this.port, this.fhost, this.fport);
        this.broker.addClient(client);
        client.start();
    }

    public void go(int port) {
    }
}

