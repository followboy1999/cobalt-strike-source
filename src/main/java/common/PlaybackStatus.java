package common;

import java.io.Serializable;

public class PlaybackStatus implements Serializable {
    protected String message;
    protected int total;
    protected int sent = 0;

    public PlaybackStatus copy() {
        PlaybackStatus temp = new PlaybackStatus(this.message, this.total);
        temp.sent = this.sent;
        return temp;
    }

    public PlaybackStatus(String message, int total) {
        this.total = total;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void message(String m) {
        this.message = m;
    }

    public void more(int crap) {
        this.total += crap;
    }

    public int getSent() {
        return this.sent;
    }

    public int getTotal() {
        return this.total;
    }

    public void sent() {
        ++this.sent;
    }

    public int percentage() {
        return (int) ((double) this.sent / (double) this.total * 100.0);
    }

    public boolean isDone() {
        return this.sent == this.total;
    }

    public boolean isStart() {
        return this.sent == 0;
    }
}

