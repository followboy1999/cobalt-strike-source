package common;

import dialog.DialogUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class DownloadFiles implements DownloadNotify {
    protected Iterator queue;
    protected File dest;
    protected TeamQueue conn;

    public void startNextDownload() {
        if (!this.queue.hasNext()) {
            DialogUtils.showInfo("Download complete!");
            return;
        }
        Map row = (Map) this.queue.next();
        String lpath = (String) row.get("lpath");
        String rname = (String) row.get("name");
        new DownloadFile(this.conn, lpath, CommonUtils.SafeFile(this.dest, rname), this).start();
    }

    public DownloadFiles(TeamQueue conn, Map[] files, File dest) {
        this.conn = conn;
        this.queue = CommonUtils.toList(files).iterator();
        this.dest = dest;
        dest.mkdirs();
    }

    @Override
    public void complete(String name) {
        this.startNextDownload();
    }

    @Override
    public void cancel() {
    }
}

