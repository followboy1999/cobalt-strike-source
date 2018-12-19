package aggressor;

import common.Callback;
import common.CommonUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowCleanup implements ActionListener,
        WindowListener {
    protected String[] keys;
    protected Callback listener;
    protected GenericDataManager data;
    protected boolean open = true;

    public WindowCleanup(GenericDataManager data, String keys, Callback l) {
        this.keys = CommonUtils.toArray(keys);
        this.listener = l;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (String key : this.keys) {
            this.data.unsub(key, this.listener);
        }
        this.open = false;
    }

    public boolean isOpen() {
        return this.open;
    }

    @Override
    public void windowClosed(WindowEvent e) {
        for (String key : this.keys) {
            this.data.unsub(key, this.listener);
        }
        this.open = false;
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
}

