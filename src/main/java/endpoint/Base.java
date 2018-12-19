package endpoint;

import tap.TapProtocol;

public abstract class Base {
    protected TapProtocol tap;
    protected long rx;
    protected long tx;
    protected FrameReader r;

    public Base(TapProtocol tapProtocol) {
        this.tap = tapProtocol;
    }

    public void start() {
        this.r = new FrameReader();
        new Thread(this.r).start();
    }

    public void setHWAddress(byte[] arrby) {
        this.tap.setHWAddress(arrby);
    }

    public abstract void processFrame(byte[] var1);

    public long getTransmittedBytes() {
        return this.tx;
    }

    public long getReceivedBytes() {
        return this.rx;
    }

    public abstract void shutdown();

    public void quit() {
        if (!(this.tap.isStopped() || "disconnected".equals(this.tap.getRemoteHost()) || "not connected".equals(this.tap.getRemoteHost()) || this.tap.getRemoteHost() == null)) {
            this.processFrame(this.tap.readKillFrame());
        }
        this.stop();
    }

    public void stop() {
        this.tap.setRemoteHost("disconnected");
        this.tap.stop();
        this.shutdown();
    }

    private class FrameReader implements Runnable {
        public boolean going = true;

        private FrameReader() {
        }

        @Override
        public void run() {
            while (!Base.this.tap.isStopped()) {
                byte[] arrby = Base.this.tap.readFrame();
                Base.this.tx += (long) arrby.length;
                Base.this.processFrame(arrby);
            }
        }
    }

}

