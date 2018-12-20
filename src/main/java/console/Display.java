package console;

import aggressor.Prefs;
import common.CommonUtils;
import dialog.DialogUtils;
import ui.CopyPopup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.Properties;

public class Display extends JPanel {
    protected JTextPane console;
    protected Properties display;
    protected Font consoleFont;
    protected Colors colors;
    protected LinkedList<JComponent> components = new LinkedList<>();

    private void updateComponentLooks() {
        this.colors = new Colors(this.display);
        Color foreground = Prefs.getPreferences().getColor("console.foreground.color", "#ffffff");
        Color background = Prefs.getPreferences().getColor("console.background.color", "#000000");
        for (JComponent component : this.components) {
            if (component == this.console) {
                component.setOpaque(false);
            } else {
                component.setBackground(background);
            }
            component.setForeground(foreground);
            component.setFont(this.consoleFont);
            if (component == this.console) {
                component.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            } else {
                component.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
            if (!(component instanceof JTextComponent)) continue;
            JTextComponent tcomponent = (JTextComponent) component;
            tcomponent.setCaretColor(foreground.brighter());
        }
    }

    public void append(final String text) {
        CommonUtils.runSafe(() -> Display.this._append(text));
    }

    public void _append(String text) {
        Rectangle r = this.console.getVisibleRect();
        this.colors.append(this.console, text);
        this.console.scrollRectToVisible(r);
    }

    public void setText(final String _text) {
        CommonUtils.runSafe(() -> Display.this.console.setText(_text));
    }

    public void setTextDirect(String _text) {
        this.console.setText(_text);
    }

    public Display() {
        this(new Properties());
    }

    public Display(Properties display) {
        this.display = display;
        this.consoleFont = Prefs.getPreferences().getFont("console.font.font", "Monospaced BOLD 14");
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(2, 2, 2, 2));
        this.console = new JTextPane();
        this.console.setEditable(false);
        this.console.setCaret(new DefaultCaret() {

            @Override
            public void setSelectionVisible(boolean visible) {
                super.setSelectionVisible(true);
            }
        });
        JScrollPane scroll = new JScrollPane(this.console, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scroll, "Center");
        this.components.add(this.console);
        this.components.add(scroll);
        this.components.add(this);
        this.updateComponentLooks();
        new CopyPopup(this.console);
        this.addActionForKeySetting("console.clear_screen.shortcut", "ctrl K", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Display.this.console.setText("");
            }
        });
        this.addActionForKeySetting("console.select_all.shortcut", "ctrl A", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Display.this.console.requestFocus();
                Display.this.console.selectAll();
            }
        });
        this.setupFindShortcutFeature();
        this.setupPageShortcutFeature();
        this.setupFontShortcutFeature();
        this.console.setBackground(new Color(0, 0, 0, 0));
        Color background = Prefs.getPreferences().getColor("console.background.color", "#000000");
        scroll.getViewport().setBackground(background);
        this.console.setOpaque(false);
    }

    private void setupFindShortcutFeature() {
        Properties myDisplay = this.display;
        final Display myConsole = this;
        this.addActionForKeySetting("console.find.shortcut", "ctrl pressed F", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Color highlight = Prefs.getPreferences().getColor("console.highlight.color", "#0000cc");
                final SearchPanel search = new SearchPanel(Display.this.console, highlight);
                final JPanel north = new JPanel();
                JButton goaway = new JButton("X ");
                DialogUtils.removeBorderFromButton(goaway);
                goaway.addActionListener(ev1 -> {
                    myConsole.remove(north);
                    myConsole.validate();
                    search.clear();
                });
                north.setLayout(new BorderLayout());
                north.add(search, "Center");
                north.add(goaway, "East");
                myConsole.add(north, "North");
                myConsole.validate();
                search.requestFocusInWindow();
                search.requestFocus();
            }

        });
    }

    private void setupFontShortcutFeature() {
        this.addActionForKeySetting("console.font_size_plus.shortcut", "ctrl EQUALS", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Display.this.changeFontSize(1.0f);
            }
        });
        this.addActionForKeySetting("console.font_size_minus.shortcut", "ctrl MINUS", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Display.this.changeFontSize(-1.0f);
            }
        });
        this.addActionForKeySetting("console.font_size_reset.shortcut", "ctrl pressed 0", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Display.this.consoleFont = Prefs.getPreferences().getFont("console.font.font", "Monospaced BOLD 14");
                Display.this.updateComponentLooks();
            }
        });
    }

    private void setupPageShortcutFeature() {
        this.addActionForKeySetting("console.page_up.shortcut", "pressed PAGE_UP", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Rectangle visible = new Rectangle(Display.this.console.getVisibleRect());
                Rectangle scrollme = new Rectangle(0, (int) (visible.getY() - visible.getHeight() / 2.0), 1, 1);
                if (scrollme.getY() <= 0.0) {
                    visible.setLocation(0, 0);
                }
                Display.this.console.scrollRectToVisible(scrollme);
            }
        });
        this.addActionForKeySetting("console.page_down.shortcut", "pressed PAGE_DOWN", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Rectangle visible = new Rectangle(Display.this.console.getVisibleRect());
                Rectangle scrollme = new Rectangle(0, (int) (visible.getY() + visible.getHeight() + visible.getHeight() / 2.0), 1, 1);
                if (scrollme.getY() >= (double) Display.this.console.getHeight()) {
                    visible.setLocation(0, Display.this.console.getHeight());
                }
                Display.this.console.scrollRectToVisible(scrollme);
            }
        });
    }

    private void changeFontSize(float difference) {
        this.consoleFont = this.consoleFont.deriveFont(this.consoleFont.getSize2D() + difference);
        this.updateComponentLooks();
    }

    public void addActionForKeyStroke(KeyStroke key, Action action) {
        this.console.getKeymap().addActionForKeyStroke(key, action);
    }

    public void addActionForKey(String key, Action action) {
        this.addActionForKeyStroke(KeyStroke.getKeyStroke(key), action);
    }

    public void addActionForKeySetting(String key, String dvalue, Action action) {
        KeyStroke temp = KeyStroke.getKeyStroke(this.display.getProperty(key, dvalue));
        if (temp != null) {
            this.addActionForKeyStroke(temp, action);
        }
    }

    public void clear() {
        CommonUtils.Guard();
        this.console.setDocument(new DefaultStyledDocument());
    }

    public void swap(StyledDocument doc) {
        CommonUtils.Guard();
        this.console.setDocument(doc);
    }

    public JTextPane getConsole() {
        return this.console;
    }

}

