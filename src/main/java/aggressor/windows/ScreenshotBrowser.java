package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import common.*;
import cortana.Cortana;
import dialog.DialogUtils;
import ui.DataBrowser;
import ui.DataSelectionListener;
import ui.ZoomableImage;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;

public class ScreenshotBrowser
        extends AObject implements AdjustData,
        DataSelectionListener {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected DataBrowser browser = null;
    protected ZoomableImage viewer = null;

    public ScreenshotBrowser(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("screenshots", this);
    }

    public JComponent getContent() {
        LinkedList screens = this.data.populateAndSubscribe("screenshots", this);
        this.viewer = new ZoomableImage();
        this.browser = DataBrowser.getBeaconDataBrowser(this.engine, "data", new JScrollPane(this.viewer), screens);
        this.browser.addDataSelectionListener(this);
        DialogUtils.setupDateRenderer(this.browser.getTable(), "when");
        return this.browser;
    }

    @Override
    public void selected(Object o) {
        if (o != null) {
            this.viewer.setIcon(((Screenshot) o).getImage());
        } else {
            this.viewer.setIcon(null);
        }
    }

    @Override
    public Map<String, Object> format(String key, Object _screen) {
        Screenshot screen = (Screenshot) _screen;
        BeaconEntry entry = DataUtils.getBeacon(this.data, screen.id());
        if (entry == null) {
            return null;
        }
        Map<String, Object> result = entry.toMap();
        result.put("when", screen.time());
        result.put("data", screen);
        return result;
    }

    @Override
    public void result(String key, Object o) {
        if (this.browser == null) {
            return;
        }
        Map<String, Object> res = this.format(key, o);
        if (res != null) {
            this.browser.addEntry(res);
        }
    }
}

