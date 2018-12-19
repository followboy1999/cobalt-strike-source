package stagers;

import common.Listener;

public class ForeignDNSStagerX86
        extends GenericDNSStagerX86 {
    public ForeignDNSStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/foreign/reverse_dns_txt";
    }

    @Override
    public String getDNSHost() {
        return this.getListener().getHost();
    }
}

