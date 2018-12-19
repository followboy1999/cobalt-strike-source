package kerberos;

import common.DataParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Principal {
    protected int name_type;
    protected String realm;
    protected List components = new LinkedList();

    public Principal(int name_type, String realm) {
        this.name_type = name_type;
        this.realm = realm;
    }

    public Principal(DataParser parser) throws IOException {
        this.name_type = parser.readInt();
        int num_components = parser.readInt();
        this.realm = parser.readCountedString();
        for (int x = 0; x < num_components; ++x) {
            this.components.add(parser.readCountedString());
        }
    }

    public String toString() {
        return "Principal(" + this.name_type + ") " + this.realm + " " + this.components;
    }
}

