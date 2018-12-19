package socks;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class PortForward implements Runnable,
        Mortal {
    protected ServerSocket server = null;
    protected SocksProxy broker;
    protected String fhost;
    protected int fport;
    protected int port = 0;

    @Override
    public void die() {
        try {
            if (this.server != null) {
                this.server.close();
            }
        } catch (IOException ioex) {
            // empty catch block
        }
    }

    @Override
    public Map toMap() {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("type", "port forward");
        temp.put("port", this.port + "");
        temp.put("fhost", this.fhost);
        temp.put("fport", this.fport + "");
        return temp;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public PortForward(SocksProxy broker, String host, int port) {
        this.broker = broker;
        this.fhost = host;
        this.fport = port;
    }

    public void go(int port) throws IOException {
        this.server = new ServerSocket(port, 128);
        this.port = port;
        new Thread(this, "PortForward 0.0.0.0:" + port + " -> " + this.fhost + ":" + this.fport).start();
    }

    private void waitForClient(ServerSocket server) throws IOException {
        Socket client = server.accept();
        client.setKeepAlive(true);
        client.setSoTimeout(0);
        this.broker.addClient(new PortForwardClient(this.broker, client, this.broker.nextId(), this.fhost, this.fport));
    }

    @Override
    public void run() {
        try {
            this.server.setSoTimeout(0);
            do {
                this.waitForClient(this.server);
            } while (true);
        } catch (IOException ioex) {
            return;
        }
    }
}

