package socks;

import java.io.*;

public class SocksCommand {
    public static final int COMMAND_CONNECT = 1;
    public static final int COMMAND_BIND = 2;
    public static final int REQUEST_GRANTED = 90;
    public static final int REQUEST_FAILED = 91;
    protected int version;
    protected int command;
    protected int dstport;
    protected int dstraw;
    protected String dstip;
    protected String userid;

    public static String toHost(long ip) {
        long a = ((ip & -16777216L) >>> 24) % 255L;
        long b = ((ip & 0xFF0000L) >>> 16) % 255L;
        long c = ((ip & 65280L) >>> 8) % 255L;
        long d = (ip & 255L) % 255L;
        return a + "." + b + "." + c + "." + d;
    }

    public void reply(OutputStream o, int reply) throws IOException {
        if (reply != 90 && reply != 91) {
            throw new IllegalArgumentException("invalid SOCKS reply: " + reply);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
        DataOutputStream data = new DataOutputStream(buffer);
        data.writeByte(0);
        data.writeByte(reply);
        data.writeShort(this.dstport);
        if (this.getCommand() == 2) {
            data.writeInt(0);
        } else {
            data.writeInt(this.dstraw);
        }
        o.write(buffer.toByteArray());
    }

    public String toString() {
        return "[version: " + this.version + ", command: " + this.command + ", dstip: " + this.dstip + ", dstport: " + this.dstport + ", userid: " + this.userid + "]";
    }

    public int getVersion() {
        return this.version;
    }

    public int getCommand() {
        return this.command;
    }

    public String getHost() {
        return this.dstip;
    }

    public int getPort() {
        return this.dstport;
    }

    protected String readString(DataInputStream data) throws IOException {
        byte next;
        StringBuilder temp = new StringBuilder();
        while ((next = data.readByte()) != 0) {
            temp.append((char) next);
        }
        return temp.toString();
    }

    public SocksCommand(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        this.version = data.readUnsignedByte();
        if (this.version != 4) {
            throw new IOException("invalid SOCKS version: " + this.version);
        }
        this.command = data.readUnsignedByte();
        if (this.command != 1 && this.command != 2) {
            throw new IOException("invalid SOCKS command: " + this.command);
        }
        this.dstport = data.readUnsignedShort();
        this.dstraw = data.readInt();
        this.userid = this.readString(data);
        this.dstip = (this.dstraw & -256) == 0 ? this.readString(data) : SocksCommand.toHost(this.dstraw);
    }
}

