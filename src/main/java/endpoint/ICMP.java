package endpoint;

import icmp.Server;
import tap.TapProtocol;

import java.io.*;
import java.util.LinkedList;

public class ICMP
        extends Base implements Server.IcmpListener {
    public static final short COMMAND_READ = 204;
    public static final short COMMAND_WRITE = 221;
    protected byte[] buffer = new byte[1048576];
    protected LinkedList outframes = new LinkedList();
    protected int outsize = 0;

    public ICMP(TapProtocol tapProtocol) {
        super(tapProtocol);
        this.start();
    }

    @Override
    public void processFrame(byte[] arrby) {
        synchronized (this) {
            this.outframes.add(new Snapshot(arrby));
            this.outsize += arrby.length + 2;
            while (this.outsize > 8192) {
                Snapshot snapshot = (Snapshot) this.outframes.removeFirst();
                this.outsize -= snapshot.data.length + 2;
            }
        }
    }

    public void processWrite(byte[] arrby, int n, DataInputStream dataInputStream) throws IOException {
        while (n < arrby.length && arrby.length > 1) {
            int n2 = dataInputStream.readUnsignedShort();
            if (n + n2 > arrby.length) {
                System.err.println("#########Next read " + n + ": " + n2 + " exceeds " + arrby.length);
                return;
            }
            n += n2 + 2;
            dataInputStream.readFully(this.buffer, 0, n2);
            this.tap.writeFrame(this.buffer, n2);
            this.rx += (long) n2;
        }
    }

    public byte[] processRead(byte[] arrby, int n, DataInputStream dataInputStream) throws IOException {
        int n2 = 0;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(this.outsize);
        synchronized (this) {
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(0);
            while (this.outframes.size() > 0) {
                Snapshot snapshot = (Snapshot) this.outframes.removeFirst();
                dataOutputStream.writeShort(snapshot.data.length);
                dataOutputStream.write(snapshot.data, 0, snapshot.data.length);
                n2 += snapshot.data.length + 2;
            }
            this.outsize -= n2;
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public byte[] icmp_ping(String string, byte[] arrby) {
        if (this.tap.isStopped()) {
            return null;
        }
        this.tap.setRemoteHost(string);
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrby, 0, arrby.length));
        int n = 1;
        try {
            short s = dataInputStream.readShort();
            n += 2;
            if (s == 221) {
                this.processWrite(arrby, n, dataInputStream);
                return this.processRead(arrby, n, dataInputStream);
            }
            if (s == 204) {
                return this.processRead(arrby, n, dataInputStream);
            }
            System.err.println("INVALID ICMP COMMAND: " + s + " len: " + arrby.length);
            return new byte[0];
        } catch (IOException iOException) {
            iOException.printStackTrace();
            return null;
        }
    }

    @Override
    public void shutdown() {
    }

    private static final class Snapshot {
        byte[] data;

        public Snapshot(byte[] arrby) {
            this.data = arrby;
        }
    }

}

