package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TableClickListener
        extends MouseAdapter {
    protected TablePopup popup = null;

    public void setPopup(TablePopup popup) {
        this.popup = popup;
    }

    @Override
    public void mousePressed(MouseEvent ev) {
        this.checkPopup(ev);
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
        this.checkPopup(ev);
    }

    public void checkPopup(MouseEvent ev) {
        if (ev.isPopupTrigger() && this.popup != null) {
            this.popup.showPopup(ev);
        }
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
        this.checkPopup(ev);
    }
}

