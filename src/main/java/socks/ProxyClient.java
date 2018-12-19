package socks;

import java.io.IOException;
import java.net.Socket;

public class ProxyClient
        extends BasicClient {
    protected SocksCommand command = null;

    public ProxyClient(SocksProxy parent, Socket client, int chid) {
        super(parent, client, chid);
    }

    @Override
    public void start() {
        try {
            this.command.reply(this.out, 90);
            this.started = true;
        } catch (IOException ioex) {
            this.die();
            return;
        }
        super.start();
    }

    @Override
    protected void deny() {
        try {
            this.command.reply(this.out, 91);
            super.deny();
        } catch (IOException ioex) {
            // empty catch block
        }
    }

    @Override
    public void run() {
        try {
            this.setup();
            this.command = new SocksCommand(this.in);
            if (this.command.getCommand() == 1) {
                this.parent.fireEvent(ProxyEvent.EVENT_CONNECT(this.chid, this.command.getHost(), this.command.getPort()));
            } else {
                this.parent.fireEvent(ProxyEvent.EVENT_LISTEN(this.chid, this.command.getHost(), this.command.getPort()));
            }
        } catch (IOException ioe) {
            try {
                this.client.close();
            } catch (IOException ioex) {
                // empty catch block
            }
            return;
        }
    }
}

