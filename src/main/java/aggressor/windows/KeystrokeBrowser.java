package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import common.*;
import console.Colors;
import console.Display;
import cortana.Cortana;
import dialog.DialogUtils;
import ui.DataBrowser;
import ui.DataSelectionListener;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public class KeystrokeBrowser
        extends AObject implements AdjustData,
        DataSelectionListener {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected DataBrowser browser = null;
    protected Display content = null;
    protected Map<String, Map<String, Object>> sessions = new HashMap<>();
    protected Colors colors = new Colors(new Properties());

    public KeystrokeBrowser(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("keystrokes", this);
    }

    public JComponent getContent() {
        this.data.populateAndSubscribe("keystrokes", this);
        LinkedList<Map<String, Object>> history = new LinkedList<>(this.sessions.values());
        this.content = new Display(new Properties());
        this.browser = DataBrowser.getBeaconDataBrowser(this.engine, "document", this.content, history);
        this.browser.addDataSelectionListener(this);
        DialogUtils.setupDateRenderer(this.browser.getTable(), "when");
        return this.browser;
    }

    @Override
    public void selected(Object o) {
        if (o != null) {
            StyledDocument doc = (StyledDocument) o;
            this.content.swap(doc);
            this.content.getConsole().setCaretPosition(doc.getLength());
        } else {
            this.content.clear();
        }
    }

    @Override
    public Map<String, Object> format(String key, Object _keys) {
        final Keystrokes keys = (Keystrokes) _keys;
        if (!this.sessions.containsKey(keys.id())) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, keys.id());
            if (entry == null) {
                return null;
            }
            Map result = entry.toMap();
            result.put("document", new DefaultStyledDocument());
            this.sessions.put(keys.id(), result);
        }
        Map<String, Object> session = (Map<String, Object>) this.sessions.get(keys.id());
        final StyledDocument document = (StyledDocument) session.get("document");
        CommonUtils.runSafe(() -> {
            KeystrokeBrowser.this.colors.append(document, keys.getKeystrokes());
            if (KeystrokeBrowser.this.content != null && document == KeystrokeBrowser.this.content.getConsole().getDocument()) {
                KeystrokeBrowser.this.content.getConsole().scrollRectToVisible(new Rectangle(0, KeystrokeBrowser.this.content.getConsole().getHeight() + 1, 1, 1));
            }
        });
        session.put("when", keys.time());
        return session;
    }

    @Override
    public void result(String key, Object o) {
        this.format(key, o);
        if (this.browser == null) {
            return;
        }
        this.browser.setTable(this.sessions.values());
    }

}

