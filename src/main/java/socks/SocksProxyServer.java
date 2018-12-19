package socks;

import common.MudgeSanity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocksProxyServer implements Runnable,
        Mortal {
    protected ServerSocket server = null;
    protected SocksProxy broker;
    protected int port = 0;

    @Override
    public void die() {
        try {
            if (this.server != null) {
                this.server.close();
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("die: " + this.port, ioex, false);
        }
    }

    @Override
    public Map toMap() {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("type", "SOCKS4a Proxy");
        temp.put("port", this.port + "");
        return temp;
    }

    public SocksProxyServer(SocksProxy broker) {
        this.broker = broker;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public void go(int port) throws IOException {
        this.server = new ServerSocket(port, 128);
        this.port = port;
        new Thread(this, "SOCKS4a on " + port).start();
    }

    private void waitForClient(ServerSocket server) throws IOException {
        Socket client = server.accept();
        client.setKeepAlive(true);
        client.setSoTimeout(0);
        this.broker.addClient(new ProxyClient(this.broker, client, this.broker.nextId()));
    }

    @Override
    public void run() {
        try {
            this.server.setSoTimeout(0);
            do {
                this.waitForClient(this.server);
            } while (true);
        } catch (IOException ioex) {
            MudgeSanity.logException("run: " + this.port, ioex, false);
            return;
        }
    }
}

