package stagers;

import common.Listener;

public class BeaconHTTPStagerX64
        extends GenericHTTPStagerX64 {
    public BeaconHTTPStagerX64(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/beacon_http/reverse_http";
    }

    @Override
    public String getURI() {
        return this.getConfig().getURI_X64() + this.getConfig().getQueryString();
    }
}

