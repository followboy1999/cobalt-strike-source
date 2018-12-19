package common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloadFileSimple
        extends AObject implements Callback {
    protected FileOutputStream out = null;
    protected File file;
    protected TeamQueue conn;
    protected DownloadNotify listener;
    protected String rpath;

    public DownloadFileSimple(TeamQueue conn, String rpath, File file, DownloadNotify listener) {
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
            this.out = new FileOutputStream(this.file, false);
            this.conn.call("download.get", CommonUtils.args(msg.id()), this);
        } else if (msg.getType() == 1) {
            this.out.write(msg.getData());
            this.conn.call("download.get", CommonUtils.args(msg.id()), this);
        } else if (msg.getType() == 2) {
            this.out.close();
            if (this.listener != null) {
                this.listener.complete(this.file.getAbsolutePath());
            }
        } else if (msg.getType() == 3) {
            if (this.out != null) {
                this.out.close();
                if (this.listener != null) {
                    this.listener.cancel();
                }
            } else {
                CommonUtils.print_error("download sync " + this.rpath + " failed: " + msg.getError());
            }
        }
    }

    public void start() {
        this.conn.call("download.start", CommonUtils.args(this.rpath), this);
    }
}

