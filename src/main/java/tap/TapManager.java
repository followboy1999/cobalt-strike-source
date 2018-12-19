package tap;

public class TapManager {
    protected boolean stopped = false;
    protected int fd;
    protected byte[] buffer = new byte[65536];
    protected String ifname;

    public static void main(String[] arrstring) {
        System.loadLibrary("tapmanager");
        TapManager tapManager = new TapManager(arrstring[0]);
        byte[] arrby = new byte[65536];
        do {
            int n = tapManager.readFrame(arrby);
            System.err.println("Read " + n + " bytes");
        } while (true);
    }

    public String getInterface() {
        return this.ifname;
    }

    public TapManager(String string) {
        this.ifname = string;
        this.fd = this.startTap(string);
        if (this.fd < 0) {
            throw new RuntimeException("Could not allocate tap: " + this.fd);
        }
    }

    public native int startTap(String var1);

    protected native int readFrame(int var1, int var2, byte[] var3);

    public byte[] readFrame() {
        int n = this.readFrame(this.buffer);
        byte[] arrby = new byte[n];
        System.arraycopy(this.buffer, 0, arrby, 0, n);
        return arrby;
    }

    public int readFrame(byte[] arrby) {
        return this.readFrame(this.fd, 0, arrby);
    }

    protected native void writeFrame(int var1, byte[] var2, int var3);

    protected native void setHWAddress(int var1, byte[] var2);

    protected native void stopInterface(int var1);

    public void stop() {
        this.stopped = true;
        this.stopInterface(this.fd);
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public void setHWAddress(byte[] arrby) {
        if (arrby.length != 6) {
            throw new IllegalArgumentException("Hardware Address must be 6 bytes");
        }
        this.setHWAddress(this.fd, arrby);
    }

    public void writeFrame(byte[] arrby, int n) {
        if (n > arrby.length || n > 65535) {
            throw new IllegalArgumentException("Bad frame size");
        }
        this.writeFrame(this.fd, arrby, n);
    }
}

