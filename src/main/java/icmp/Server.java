package icmp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class Server implements Runnable {
    protected final HashMap<String, IcmpListener> listeners = new HashMap<>();

    public void addIcmpListener(String string, IcmpListener icmpListener) {
        synchronized (this.listeners) {
            this.listeners.put(string, icmpListener);
        }
    }

    public Server() {
        System.err.println("\u001b[01;33m[!]\u001b[0m Disabled ICMP replies for this host.");
        System.err.println("\tTo undo this: sysctl -w net.ipv4.icmp_echo_ignore_all=0");
        try {
            Runtime.getRuntime().exec("sysctl -w net.ipv4.icmp_echo_ignore_all=1");
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }
        new Thread(this).start();
    }

    public String toHost(byte[] arrby) {
        StringBuilder StringBuilder = new StringBuilder();
        for (int i = 0; i < arrby.length && arrby[i] != 0; ++i) {
            StringBuilder.append((char) arrby[i]);
        }
        return StringBuilder.toString();
    }

    @Override
    public void run() {
        byte[] arrby = new byte[128];
        byte[] arrby2 = new byte[65536];
        while (true) {
            int n;
            if ((n = this.recv_icmp(arrby, arrby2)) <= 4) {
                continue;
            }
            byte[] arrby3 = Arrays.copyOfRange(arrby2, 4, n);
            String string = this.toHost(arrby);
            String string2 = new String(arrby2, 0, 4);
            synchronized (this.listeners) {
                byte[] arrby4;
                IcmpListener icmpListener = this.listeners.get(string2);
                if (icmpListener == null) {
                    icmpListener = this.listeners.get("foob");
                }
                if (icmpListener != null && (arrby4 = icmpListener.icmp_ping(string, arrby3)) != null) {
                    this.reply_icmp(arrby4);
                }
            }
        }
    }

    protected native int recv_icmp(byte[] var1, byte[] var2);

    protected native void reply_icmp(byte[] var1);

    public static void main(String[] arrstring) throws Exception {
        System.loadLibrary("icmp");
        Server server = new Server();
        server.addIcmpListener("foob", (string, arrby) -> {
            System.err.println("Received: " + new String(arrby));
            System.err.println("From:     " + string);
            return "hey, this is a reply".getBytes();
        });
        while (true) Thread.sleep(1000L);
    }

    public interface IcmpListener {
        byte[] icmp_ping(String var1, byte[] var2);
    }

}

