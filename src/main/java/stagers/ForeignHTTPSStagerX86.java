package stagers;

import common.CommonUtils;
import common.Listener;

public class ForeignHTTPSStagerX86
        extends GenericHTTPSStagerX86 {
    public ForeignHTTPSStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/foreign/reverse_https";
    }

    @Override
    public String getURI() {
        return CommonUtils.MSFURI();
    }
}

