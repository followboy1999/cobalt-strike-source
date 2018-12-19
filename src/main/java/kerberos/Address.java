package kerberos;

import common.DataParser;

import java.io.IOException;

public class Address {
    protected int addrtype;
    protected String addrdata;

    public Address(DataParser parser) throws IOException {
        this.addrtype = parser.readShort();
        this.addrdata = parser.readCountedString();
    }

    public String toString() {
        return this.addrtype + "/" + this.addrdata;
    }
}

