package beacon.dns;

import dns.DNSServer;

public abstract class SendConversation {
    protected String id;
    protected String dtype;
    protected long idlemask;

    public SendConversation(String id, String dtype, long idlemask) {
        this.id = id;
        this.dtype = dtype;
        this.idlemask = idlemask;
    }

    public abstract boolean started();

    public abstract DNSServer.Response start(byte[] var1);

    public abstract DNSServer.Response next();

    public abstract boolean isComplete();
}

