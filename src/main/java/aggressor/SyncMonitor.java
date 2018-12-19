package aggressor;

import common.Callback;
import common.PlaybackStatus;

import javax.swing.*;

public class SyncMonitor implements Callback {
    protected ProgressMonitor monitor = null;
    protected AggressorClient client;

    public SyncMonitor(AggressorClient client) {
        this.client = client;
        client.getData().subscribe("playback.status", this);
    }

    @Override
    public void result(String key, Object result) {
        PlaybackStatus status = (PlaybackStatus) result;
        if (status.isStart()) {
            this.monitor = new ProgressMonitor(this.client, "Sync to Team Server", status.getMessage(), 0, 100);
        } else if (status.isDone() && this.monitor != null) {
            this.monitor.close();
        } else if (this.monitor != null) {
            this.monitor.setNote("[" + status.getSent() + "/" + status.getTotal() + "] " + status.getMessage());
            this.monitor.setProgress(status.percentage());
        }
    }
}

