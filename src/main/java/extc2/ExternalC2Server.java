package extc2;

import beacon.BeaconSetup;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ExternalC2Server implements Runnable {
    protected int bindport;
    protected String bindaddr;
    protected ServerSocket server = null;
    protected boolean running = true;
    protected BeaconSetup setup;

    public void die() {
        try {
            if (this.server != null) {
                this.server.close();
            }
        } catch (IOException ex) {
            MudgeSanity.logException("stop server", ex, false);
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public ExternalC2Server(BeaconSetup setup, int port) {
        this(setup, "0.0.0.0", port);
    }

    public ExternalC2Server(BeaconSetup setup, String addr, int port) {
        this.bindaddr = addr;
        this.bindport = port;
        this.setup = setup;
        new Thread(this, "External C2 Server " + addr + ":" + port).start();
    }

    private void waitForClient(ServerSocket server) throws IOException {
        Socket client = server.accept();
        client.setKeepAlive(true);
        client.setSoTimeout(0);
        new ExternalC2Session(this.setup, client);
    }

    @Override
    public void run() {
        try {
            this.server = new ServerSocket(this.bindport, 128, InetAddress.getByName(this.bindaddr));
            this.server.setSoTimeout(0);
            CommonUtils.print_good("External C2 Server up on " + this.bindaddr + ":" + this.bindport);
            do {
                this.waitForClient(this.server);
            } while (true);
        } catch (Exception ex) {
            MudgeSanity.logException("External C2 Server Accept Loop", ex, false);
            this.running = false;
            return;
        }
    }
}

