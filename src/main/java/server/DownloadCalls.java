package server;

import common.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DownloadCalls implements ServerHook {
    protected Resources resources;
    protected long ids = 0L;
    protected Map sessions = new HashMap();

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("download.start", this);
        calls.put("download.get", this);
    }

    public DownloadCalls(Resources r) {
        this.resources = r;
    }

    public String makeSession(Request r, ManageUser client, File temp) {
        try {
            FileInputStream in = new FileInputStream(temp);
            synchronized (this) {
                ++this.ids;
                this.sessions.put(this.ids + "", in);
            }
            return this.ids + "";
        } catch (IOException ioex) {
            MudgeSanity.logException("makeSession", ioex, false);
            client.writeNow(r.reply(DownloadMessage.Error(null, ioex.getMessage())));
            return null;
        }
    }

    public void getChunk(Request r, ManageUser client, String id) {
        block10:
        {
            FileInputStream in;
            synchronized (this) {
                in = (FileInputStream) this.sessions.get(id);
            }
            if (in == null) {
                client.writeNow(r.reply(DownloadMessage.Error(id, "invalid download ID")));
                return;
            }
            try {
                byte[] chunk = new byte[262144];
                int read2 = in.read(chunk);
                if (read2 > 0) {
                    byte[] send = new byte[read2];
                    System.arraycopy(chunk, 0, send, 0, read2);
                    client.writeNow(r.reply(DownloadMessage.Chunk(id, send)));
                    break block10;
                }
                synchronized (this) {
                    this.sessions.remove(id);
                    in.close();
                }
                client.writeNow(r.reply(DownloadMessage.Done(id)));
            } catch (IOException ioex) {
                MudgeSanity.logException("getChunk", ioex, false);
                client.writeNow(r.reply(DownloadMessage.Error(id, ioex.getMessage())));
            }
        }
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("download.start", 1)) {
            File temp = new File(r.arg(0) + "");
            if (!CommonUtils.isSafeFile(new File("downloads"), temp)) {
                CommonUtils.print_error(client.getNick() + " attempted to sync '" + r.arg(0) + "'. Rejected: not in the downloads/ folder.");
                client.writeNow(r.reply(DownloadMessage.Error(null, "argument is not in downloads/ folder")));
                return;
            }
            if (!temp.exists()) {
                client.writeNow(r.reply(DownloadMessage.Error(null, "File does not exist")));
                return;
            }
            String sid = this.makeSession(r, client, temp);
            if (sid == null) {
                return;
            }
            client.writeNow(r.reply(DownloadMessage.Start(sid, temp.length())));
        } else if (r.is("download.get", 1)) {
            this.getChunk(r, client, r.arg(0) + "");
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }
}

