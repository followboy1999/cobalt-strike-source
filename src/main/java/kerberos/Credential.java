package kerberos;

import common.DataParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Credential {
    protected Principal client;
    protected Principal server;
    protected KeyBlock key;
    protected Times time;
    protected byte is_skey;
    protected int tktflags;
    protected List addresses = new LinkedList();
    protected List authdata = new LinkedList();
    protected String ticket;
    protected String second_ticket;

    public Credential(DataParser p) throws IOException {
        this.client = new Principal(p);
        this.server = new Principal(p);
        this.key = new KeyBlock(p);
        this.time = new Times(p);
        this.is_skey = (byte) p.readChar();
        this.tktflags = p.readInt();
        int num_addresses = p.readInt();
        for (int x = 0; x < num_addresses; ++x) {
            this.addresses.add(new Address(p));
        }
        int num_authdata = p.readInt();
        for (int x = 0; x < num_authdata; ++x) {
            this.authdata.add(new AuthData(p));
        }
        this.ticket = p.readCountedString();
        this.second_ticket = p.readCountedString();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Credential\n");
        b.append("\tclient: ").append(this.client).append("\n");
        b.append("\tserver: ").append(this.client).append("\n");
        b.append("\tkey:    ").append(this.key).append("\n");
        b.append("\tticket: ").append(this.ticket.length()).append("\n");
        b.append("\tsecond: ").append(this.second_ticket.length()).append("\n");
        return b.toString();
    }
}

