package aggressor;

import aggressor.dialogs.PreferencesDialog;
import aggressor.dialogs.SessionChooser;
import common.AObject;
import common.CommonUtils;
import common.TabScreenshot;
import console.Activity;
import dialog.DialogUtils;
import ui.DraggableTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.LinkedList;

public class TabManager
        extends AObject {
    protected JTabbedPane tabs = new DraggableTabbedPane();
    protected ApplicationTab docked = null;
    protected LinkedList<ApplicationTab> apptabs = new LinkedList<>();
    protected AggressorClient client;

    public boolean activate(String title) {
        CommonUtils.Guard();
        for (ApplicationTab t : this.apptabs) {
            if (!title.equals(t.title)) continue;
            this.tabs.setSelectedComponent(t.component);
            return true;
        }
        return false;
    }

    public void bindShortcuts() {
        this.client.bindKey("Ctrl+I", desc -> new SessionChooser(TabManager.this.client, r -> DialogUtils.openOrActivate(TabManager.this.client, r)).show());
        this.client.bindKey("Ctrl+W", desc -> TabManager.this.openActiveTab());
        this.client.bindKey("Ctrl+B", desc -> TabManager.this.dockActiveTab());
        this.client.bindKey("Ctrl+E", desc -> TabManager.this.noDock());
        this.client.bindKey("Ctrl+D", desc -> TabManager.this.closeActiveTab());
        this.client.bindKey("Shift+Ctrl+D", desc -> TabManager.this.closeAllButActiveTab());
        this.client.bindKey("Ctrl+T", desc -> {
            TabManager.this.snapActiveTab();
            DialogUtils.showInfo("Pushed screenshot to team server (active tab)");
        });
        this.client.bindKey("Shift+Ctrl+T", desc -> {
            TabManager.this.snapActiveWindow();
            DialogUtils.showInfo("Pushed screenshot to team server (window)");
        });
        this.client.bindKey("Ctrl+Left", desc -> TabManager.this.previousTab());
        this.client.bindKey("Ctrl+Right", desc -> TabManager.this.nextTab());
        this.client.bindKey("Ctrl+O", desc -> new PreferencesDialog().show());
    }

    public TabManager(AggressorClient client) {
        this.client = client;
        this.bindShortcuts();
    }

    public JTabbedPane getTabbedPane() {
        return this.tabs;
    }

    public void _removeTab(JComponent component) {
        this.tabs.remove(component);
        this.tabs.validate();
    }

    public void removeTab(final JComponent tab) {
        CommonUtils.runSafe(() -> TabManager.this._removeTab(tab));
    }

    public void nextTab() {
        this.tabs.setSelectedIndex((this.tabs.getSelectedIndex() + 1) % this.tabs.getTabCount());
    }

    public void previousTab() {
        if (this.tabs.getSelectedIndex() == 0) {
            this.tabs.setSelectedIndex(this.tabs.getTabCount() - 1);
        } else {
            this.tabs.setSelectedIndex((this.tabs.getSelectedIndex() - 1) % this.tabs.getTabCount());
        }
    }

    public void addTab(final String title, final JComponent tab, final ActionListener removeListener) {
        CommonUtils.runSafe(() -> TabManager.this._addTab(title, tab, removeListener, null));
    }

    public void addTab(final String title, final JComponent tab, final ActionListener removeListener, final String tooltip) {
        CommonUtils.runSafe(() -> TabManager.this._addTab(title, tab, removeListener, tooltip));
    }

    public void closeActiveTab() {
        CommonUtils.Guard();
        JComponent tab = (JComponent) this.tabs.getSelectedComponent();
        if (tab != null) {
            this.removeAppTab(tab, null, new ActionEvent(tab, 0, "boo!"));
        }
    }

    public void closeAllButActiveTab() {
        CommonUtils.Guard();
        JComponent tab = (JComponent) this.tabs.getSelectedComponent();
        for (ApplicationTab t : this.apptabs) {
            if (t.component == tab) continue;
            this.removeAppTab(t.component, null, new ActionEvent(t.component, 0, "boo!"));
        }
    }

    public void openActiveTab() {
        CommonUtils.Guard();
        JComponent tab = (JComponent) this.tabs.getSelectedComponent();
        if (tab != null) {
            this.popAppTab(tab);
        }
    }

    public void noDock() {
        CommonUtils.Guard();
        if (this.docked != null) {
            if (this.docked.removeListener != null) {
                this.docked.removeListener.actionPerformed(new ActionEvent(this.docked.component, 0, "close"));
            }
            this.client.noDock();
            this.docked = null;
        }
    }

    public void dockActiveTab() {
        CommonUtils.Guard();
        JComponent tab = (JComponent) this.tabs.getSelectedComponent();
        if (tab != null) {
            this.dockAppTab(tab);
        }
    }

    public void snapActiveWindow() {
        CommonUtils.Guard();
        byte[] screenshot = DialogUtils.screenshot(this.client.getWindow());
        this.client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot(this.client.getWindow().getTitle(), screenshot)));
    }

    public void snapActiveTab() {
        CommonUtils.Guard();
        JComponent tab = (JComponent) this.tabs.getSelectedComponent();
        for (ApplicationTab t : this.apptabs) {
            if (t.component != tab) continue;
            this.snapAppTab(t.title, tab);
        }
    }

    public void addAppTab(String title, JComponent component, ActionListener removeListener) {
        CommonUtils.Guard();
        ApplicationTab t = new ApplicationTab();
        t.title = title;
        t.component = component;
        t.removeListener = removeListener;
        this.apptabs.add(t);
    }

    public void popAppTab(Component tab) {
        CommonUtils.Guard();
        Iterator i = this.apptabs.iterator();
        while (i.hasNext()) {
            final ApplicationTab t = (ApplicationTab) i.next();
            if (t.component != tab) continue;
            this.tabs.remove(t.component);
            i.remove();
            final JFrame r = new JFrame(t.title);
            r.setLayout(new BorderLayout());
            r.add(t.component, "Center");
            r.pack();
            t.component.validate();
            r.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent ev) {
                    if (t.removeListener != null) {
                        t.removeListener.actionPerformed(new ActionEvent(ev.getSource(), 0, "close"));
                    }
                }

                @Override
                public void windowOpened(WindowEvent ev) {
                    r.setState(0);
                    t.component.requestFocusInWindow();
                }

                @Override
                public void windowActivated(WindowEvent ev) {
                    t.component.requestFocusInWindow();
                }
            });
            r.setState(1);
            r.setVisible(true);
        }
    }

    public void dockAppTab(Component tab) {
        CommonUtils.Guard();
        Iterator i = this.apptabs.iterator();
        while (i.hasNext()) {
            ApplicationTab t = (ApplicationTab) i.next();
            if (t.component != tab) continue;
            this.tabs.remove(t.component);
            i.remove();
            Dimension size = new Dimension(100, 150);
            if (this.docked != null) {
                size = this.docked.component.getSize();
                if (this.docked.removeListener != null) {
                    this.docked.removeListener.actionPerformed(new ActionEvent(this.docked.component, 0, "close"));
                }
            }
            this.client.dock(t.component, size);
            this.docked = t;
        }
    }

    public void snapAppTab(String title, Component tab) {
        byte[] screenshot = DialogUtils.screenshot(tab);
        this.client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot(title, screenshot)));
    }

    public void removeAppTab(Component tab, String title, ActionEvent ev) {
        String titleshort;
        CommonUtils.Guard();
        Iterator i = this.apptabs.iterator();
        String string = titleshort = title != null ? title.split(" ")[0] : "%b%";
        while (i.hasNext()) {
            String tshort;
            ApplicationTab t = (ApplicationTab) i.next();
            String string2 = tshort = t.title != null ? t.title.split(" ")[0] : "%a%";
            if (t.component != tab && !tshort.equals(titleshort)) continue;
            this.tabs.remove(t.component);
            if (t.removeListener != null) {
                t.removeListener.actionPerformed(ev);
            }
            i.remove();
        }
    }

    public void _addTab(final String title, final JComponent tab, ActionListener removeListener, String tooltip) {
        if (removeListener == null) {
            CommonUtils.print_error("Opened: " + title + " with no remove listener");
        }
        final Component component = this.tabs.add("", tab);
        final JLabel label = new JLabel(title + "   ");
        JPanel control = new JPanel();
        control.setOpaque(false);
        control.setLayout(new BorderLayout());
        control.add(label, "Center");
        if (tab instanceof Activity) {
            ((Activity) tab).registerLabel(label);
        }
        JButton close = new JButton("X");
        close.setOpaque(false);
        close.setContentAreaFilled(false);
        close.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        control.add(close, "East");
        if (tooltip != null) {
            close.setToolTipText(tooltip);
        }
        int index = this.tabs.indexOfComponent(component);
        this.tabs.setTabComponentAt(index, control);
        this.addAppTab(title, tab, removeListener);
        close.addMouseListener(new MouseAdapter() {

            public void check(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem a = new JMenuItem("Open in window", 79);
                    a.addActionListener(ev15 -> TabManager.this.popAppTab(component));
                    JMenuItem b = new JMenuItem("Close like tabs", 67);
                    b.addActionListener(ev14 -> TabManager.this.removeAppTab(null, title, ev14));
                    JMenuItem c = new JMenuItem("Save screenshot", 83);
                    c.addActionListener(ev13 -> {
                        TabManager.this.snapAppTab(title, tab);
                        DialogUtils.showInfo("Pushed screenshot to team server");
                    });
                    JMenuItem dd = new JMenuItem("Send to bottom", 98);
                    dd.addActionListener(ev12 -> TabManager.this.dockAppTab(component));
                    JMenuItem d = new JMenuItem("Rename Tab", 82);
                    d.addActionListener(ev1 -> {
                        String text = JOptionPane.showInputDialog("Rename tab to:", (label.getText() + "").trim());
                        if (text != null) {
                            label.setText(text + "   ");
                        }
                    });
                    menu.add(a);
                    menu.add(c);
                    menu.add(dd);
                    menu.add(d);
                    menu.addSeparator();
                    menu.add(b);
                    menu.show((Component) ev.getSource(), ev.getX(), ev.getY());
                    ev.consume();
                }
            }

            @Override
            public void mouseClicked(MouseEvent ev) {
                this.check(ev);
            }

            @Override
            public void mousePressed(MouseEvent ev) {
                this.check(ev);
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                this.check(ev);
            }

        });
        close.addActionListener(ev -> {
            if ((ev.getModifiers() & 2) == 2) {
                TabManager.this.popAppTab(component);
            } else if ((ev.getModifiers() & 1) == 1) {
                TabManager.this.removeAppTab(null, title, ev);
            } else {
                TabManager.this.removeAppTab(component, null, ev);
            }
            System.gc();
        });
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ev) {
                if (component instanceof Activity) {
                    ((Activity) component).resetNotification();
                }
                component.requestFocusInWindow();
                System.gc();
            }
        });
        this.tabs.setSelectedIndex(index);
        component.requestFocusInWindow();
    }

    public void touch() {
        CommonUtils.Guard();
        Component c = this.tabs.getSelectedComponent();
        if (c == null) {
            return;
        }
        if (c instanceof Activity) {
            ((Activity) c).resetNotification();
        }
        c.requestFocusInWindow();
    }

    private static class ApplicationTab {
        public String title;
        public JComponent component;
        public ActionListener removeListener;

        private ApplicationTab() {
        }

        public String toString() {
            return this.title;
        }
    }

}

