package common;

import dialog.DialogUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadFile
        extends AObject implements Callback {
    protected FileOutputStream out = null;
    protected File file;
    protected TeamQueue conn;
    protected DownloadNotify listener;
    protected String rpath;
    protected long total = 0L;
    protected long start = System.currentTimeMillis();
    protected long read = 0L;
    protected long ret = 0L;
    protected long sofar = 0L;
    protected double time = 0.0;
    protected ProgressMonitor progress = null;

    public DownloadFile(TeamQueue conn, String rpath, File file, DownloadNotify listener) {
        this.file = file;
        this.listener = listener;
        this.conn = conn;
        this.rpath = rpath;
    }

    @Override
    public void result(String call, Object result) {
        try {
            this._result(call, result);
        } catch (IOException ioex) {
            MudgeSanity.logException(call + " " + result, ioex, false);
        }
    }

    public void _result(String call, Object result) throws IOException {
        DownloadMessage msg = (DownloadMessage) result;
        if (msg.getType() == 0) {
            this.total = msg.getSize();
            this.out = new FileOutputStream(this.file, false);
            this.progress = new ProgressMonitor(null, "Download " + this.file.getName(), "Starting download", 0, (int) this.total);
            this.conn.call("download.get", CommonUtils.args(msg.id()), this);
        } else if (msg.getType() == 1) {
            this.time = (double) (System.currentTimeMillis() - this.start) / 1000.0;
            this.sofar += (long) msg.getData().length;
            this.progress.setProgress((int) this.sofar);
            this.progress.setNote("Speed: " + Math.round((double) (this.sofar / 1024L) / this.time) + " KB/s");
            if (this.progress.isCanceled()) {
                this.progress.close();
                this.out.close();
                if (this.listener != null) {
                    this.listener.cancel();
                }
                return;
            }
            this.out.write(msg.getData());
            this.conn.call("download.get", CommonUtils.args(msg.id()), this);
        } else if (msg.getType() == 2) {
            this.progress.close();
            this.out.close();
            if (this.listener != null) {
                this.listener.complete(this.file.getAbsolutePath());
            }
        } else if (msg.getType() == 3) {
            if (this.out != null && this.progress != null) {
                this.out.close();
                this.progress.setNote(msg.getError());
                if (this.listener != null) {
                    this.listener.cancel();
                }
            } else {
                DialogUtils.showError(msg.getError());
            }
        }
    }

    public void start() {
        this.conn.call("download.start", CommonUtils.args(this.rpath), this);
    }
}

