package kerberos;

import common.DataParser;

import java.io.IOException;

public class AuthData {
    protected int authtype;
    protected String authdata;

    public AuthData(DataParser parser) throws IOException {
        this.authtype = parser.readShort();
        this.authdata = parser.readCountedString();
    }

    public String toString() {
        return this.authtype + "/" + this.authdata;
    }
}

