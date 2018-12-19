package beacon.dns;

import common.ByteIterator;
import common.CommonUtils;
import dns.DNSServer;

public class SendConversationAAAA
        extends SendConversation {
    protected ByteIterator readme = null;

    public SendConversationAAAA(String id, String dtype, long mask) {
        super(id, dtype, mask);
    }

    @Override
    public boolean started() {
        return this.readme != null;
    }

    @Override
    public DNSServer.Response start(byte[] data) {
        this.readme = new ByteIterator(data);
        return DNSServer.A((long) data.length ^ this.idlemask);
    }

    @Override
    public DNSServer.Response next() {
        byte[] chunk = this.readme.next(16L);
        if (chunk.length != 16) {
            CommonUtils.print_warn("AAAA channel: task chunk is not 16 bytes.");
        }
        return DNSServer.AAAA(chunk);
    }

    @Override
    public boolean isComplete() {
        if (this.readme == null) {
            return true;
        }
        return !this.readme.hasNext();
    }
}

