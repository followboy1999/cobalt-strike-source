package beacon;

import common.CommonUtils;
import common.Download;
import common.MudgeSanity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BeaconDownloads {
    protected LinkedList<BeaconDownload> downloads = new LinkedList<>();

    public void start(String id, int fid, String internal, String fname, long flen) {
        try {
            File dest = CommonUtils.SafeFile("downloads", CommonUtils.garbage("file name"));
            dest.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(dest, false);
            String name_only = new File(fname.replace("\\", "/")).getName();
            String path_only = CommonUtils.stripRight(fname, name_only);
            BeaconDownload download = new BeaconDownload(name_only, out, id, fid);
            download.flen = flen;
            download.rpath = path_only;
            download.lpath = dest.getCanonicalPath();
            download.host = internal;
            synchronized (this) {
                this.downloads.add(download);
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("start download: " + fname, ioex, false);
        }
    }

    protected List getDownloads(String id) {
        LinkedList<Map> entries;
        synchronized (this) {
            entries = this.downloads.stream().filter(next -> next.bid.equals(id)).map(next -> next.toDownload().toMap()).collect(Collectors.toCollection(LinkedList::new));
        }
        return entries;
    }

    protected BeaconDownload find(String id, int fid) {

        synchronized (this) {
            for (BeaconDownload next : this.downloads) {
                if (!next.is(id, fid)) continue;
                return next;
            }
        }
        return null;
    }

    public void write(String id, int fid, byte[] data) {
        synchronized (this) {
            try {
                BeaconDownload next = this.find(id, fid);
                next.handle.write(data, 0, data.length);
            } catch (IOException ioex) {
                MudgeSanity.logException("write download", ioex, false);
            }
        }
    }

    public boolean exists(String id, int fid) {
        return this.find(id, fid) != null;
    }

    public boolean isActive(String bid) {
        synchronized (this) {
            for (BeaconDownload next : this.downloads) {
                if (!next.bid.equals(bid)) continue;
                return true;
            }
        }
        return false;
    }

    public String getName(String id, int fid) {
        BeaconDownload next = this.find(id, fid);
        if (next == null) {
            return "unknown";
        }
        return next.fname;
    }

    public Download getDownload(String id, int fid) {
        BeaconDownload next = this.find(id, fid);
        if (next == null) {
            return null;
        }
        return next.toDownload();
    }

    public void close(String id, int fid) {
        synchronized (this) {
            Iterator i = this.downloads.iterator();
            while (i.hasNext()) {
                BeaconDownload next = (BeaconDownload) i.next();
                if (!next.is(id, fid)) continue;
                i.remove();
                try {
                    next.handle.close();
                } catch (IOException ioex) {
                    MudgeSanity.logException("write close", ioex, false);
                }
            }
        }
    }

    public static class BeaconDownload {
        public String fname;
        public OutputStream handle;
        public String bid;
        public int fid;
        public long start = System.currentTimeMillis();
        public long flen;
        public String rpath;
        public String lpath;
        public String host;

        public BeaconDownload(String fname, OutputStream handle, String bid, int fid) {
            this.fname = fname;
            this.handle = handle;
            this.bid = bid;
            this.fid = fid;
        }

        public boolean is(String bid, int fid) {
            return this.bid.equals(bid) && this.fid == fid;
        }

        public Download toDownload() {
            return new Download(this.fid, this.bid, this.host, this.fname, this.rpath, this.lpath, this.flen);
        }
    }

}

