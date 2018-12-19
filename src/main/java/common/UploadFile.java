package common;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UploadFile implements Callback {
    protected FileInputStream in = null;
    protected byte[] buffer = new byte[262144];
    protected File file;
    protected TeamQueue conn;
    protected UploadNotify listener;
    protected long total;
    protected long start = System.currentTimeMillis();
    protected long read = 0L;
    protected long ret = 0L;
    protected long sofar = 0L;
    protected double time = 0.0;
    protected ProgressMonitor progress;

    public UploadFile(TeamQueue conn, File file, UploadNotify listener) {
        this.file = file;
        this.listener = listener;
        this.conn = conn;
    }

    @Override
    public void result(String call, Object result) {
        String message = result + "";
        try {
            if (this.sofar < this.total) {
                this.time = (double) (System.currentTimeMillis() - this.start) / 1000.0;
                this.progress.setProgress((int) this.sofar);
                this.progress.setNote("Speed: " + Math.round((double) (this.sofar / 1024L) / this.time) + " KB/s");
                if (this.progress.isCanceled()) {
                    this.progress.close();
                    this.in.close();
                    this.listener.cancel();
                    return;
                }
                if (message.startsWith("ERROR: ")) {
                    this.progress.setNote(message);
                    this.in.close();
                    return;
                }
                this.read = this.in.read(this.buffer);
                this.sofar += this.read;
                this.conn.call("armitage.append", CommonUtils.args(this.file.getName(), this.tailor(this.buffer, this.read)), this);
            } else {
                this.time = (double) (System.currentTimeMillis() - this.start) / 1000.0;
                this.progress.setProgress((int) this.sofar);
                this.progress.setNote("Speed: " + Math.round((double) (this.sofar / 1024L) / this.time) + " KB/s");
                this.progress.close();
                this.in.close();
                this.listener.complete(result + "");
            }
        } catch (Exception ioex) {
            MudgeSanity.logException("upload" + this.sofar + "/" + this.total + " of " + this.file, ioex, false);
            this.listener.cancel();
        }
    }

    public void start() {
        try {
            this.total = this.file.length();
            this.in = new FileInputStream(this.file);
            this.progress = new ProgressMonitor(null, "Upload " + this.file.getName(), "Starting upload", 0, (int) this.total);
            this.conn.call("armitage.upload", CommonUtils.args(this.file.getName()), this);
        } catch (IOException ioex) {
            MudgeSanity.logException("upload start: " + this.file, ioex, false);
            this.listener.cancel();
        }
    }

    protected byte[] tailor(byte[] data, long length) {
        byte[] me = new byte[(int) length];
        int x = 0;
        while ((long) x < length) {
            me[x] = data[x];
            ++x;
        }
        return me;
    }

    public interface UploadNotify {
        void complete(String var1);

        void cancel();
    }

}

