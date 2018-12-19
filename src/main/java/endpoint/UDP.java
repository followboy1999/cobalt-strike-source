package endpoint;

import tap.EncryptedTap;
import tap.TapProtocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDP
        extends Base implements Runnable {
    protected DatagramSocket server;
    protected DatagramPacket in_packet = new DatagramPacket(new byte[65536], 65536);
    protected DatagramPacket out_packet = new DatagramPacket(new byte[65536], 65536);
    protected byte[] buffer = new byte[65536];

    public UDP(TapProtocol tapProtocol, int n) throws IOException {
        super(tapProtocol);
        this.server = new DatagramSocket(n);
        new Thread(this).start();
    }

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        EncryptedTap encryptedTap = new EncryptedTap("phear0", "foobar".getBytes());
        try {
            new UDP(encryptedTap, 31337);
            do {
                Thread.sleep(1000L);
            } while (true);
        } catch (InterruptedException interruptedException) {
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    @Override
    public synchronized void processFrame(byte[] arrby) {
        try {
            this.out_packet.setData(arrby);
            this.server.send(this.out_packet);
        } catch (IOException iOException) {
            this.stop();
            iOException.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            this.server.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.server.receive(this.out_packet);
            this.tap.setRemoteHost(this.out_packet.getAddress().getHostAddress());
            this.start();
            while (!this.tap.isStopped()) {
                this.server.receive(this.in_packet);
                this.rx += (long) this.in_packet.getLength();
                this.tap.writeFrame(this.in_packet.getData(), this.in_packet.getLength());
            }
        } catch (IOException iOException) {
            this.stop();
            iOException.printStackTrace();
        }
    }
}

