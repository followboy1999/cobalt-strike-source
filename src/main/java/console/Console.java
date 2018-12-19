package console;

import aggressor.Prefs;
import common.CommonUtils;
import dialog.DialogUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Pattern;

public class Console
        extends JPanel implements FocusListener {
    protected JTextPane console;
    protected JTextField input;
    protected JTextPane prompt;
    protected StatusBar status;
    protected PrintStream log = null;
    protected Properties display;
    protected Font consoleFont;
    protected Colors colors;
    protected ClickListener clickl;
    protected String defaultPrompt = "aggressor > ";
    protected LinkedList<JComponent> components = new LinkedList<>();
    protected ListIterator history = new LinkedList().listIterator(0);
    protected boolean promptLock = false;
    protected Replacements[] colorme = null;
    protected JPanel bottom;

    public void addWordClickListener(ActionListener l) {
        this.clickl.addListener(l);
    }

    public void writeToLog(PrintStream p) {
        this.log = p;
    }

    public void setDefaultPrompt(String p) {
        this.defaultPrompt = p;
    }

    public void setPopupMenu(ConsolePopup menu) {
        this.clickl.setPopup(menu);
    }

    public JTextField getInput() {
        return this.input;
    }

    public void updateProperties(Properties display) {
        this.display = display;
        this.updateComponentLooks();
    }

    private void updateComponentLooks() {
        this.colors = new Colors(this.display);
        Color foreground = Prefs.getPreferences().getColor("console.foreground.color", "#c0c0c0");
        Color background = Prefs.getPreferences().getColor("console.background.color", "#000000");
        for (JComponent component : this.components) {
            if (component == this.status) {
                component.setFont(this.consoleFont);
                continue;
            }
            component.setForeground(foreground);
            if (component == this.console || component == this.prompt) {
                component.setOpaque(false);
            } else {
                component.setBackground(background);
            }
            component.setFont(this.consoleFont);
            if (component == this.console || component == this.prompt) {
                component.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            } else {
                component.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
            if (!(component instanceof JTextComponent)) continue;
            JTextComponent tcomponent = (JTextComponent) component;
            tcomponent.setCaretColor(foreground.brighter());
        }
    }

    public String getPromptText() {
        return this.prompt.getText();
    }

    public void setPrompt(String text) {
        String bad = "\ufffd\ufffd";
        if (text.equals(bad) || text.equals("null")) {
            this.colors.set(this.prompt, this.fixText(this.defaultPrompt));
        } else {
            this.defaultPrompt = text;
            this.colors.set(this.prompt, this.fixText(text));
        }
    }

    public void updatePrompt(final String _prompt) {
        CommonUtils.runSafe(() -> {
            if (!Console.this.promptLock) {
                Console.this.setPrompt(_prompt);
            }
        });
    }

    public void setStyle(String text) {
        String[] lines = text.trim().split("\n");
        this.colorme = new Replacements[lines.length];
        for (int x = 0; x < lines.length; ++x) {
            String[] ab = lines[x].split("\\t+");
            if (ab.length == 2) {
                ab[1] = ab[1].replace("\\c", "\u0003");
                ab[1] = ab[1].replace("\\o", "\u000f");
                ab[1] = ab[1].replace("\\u", "\u001f");
                this.colorme[x] = new Replacements(ab[0], ab[1]);
                continue;
            }
            System.err.println(lines[x] + "<-- didn't split right:" + ab.length);
        }
    }

    protected String fixText(String text) {
        if (this.colorme == null) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("(?<=\\n)");
        for (String line : lines) {
            String temp = line;
            for (Replacements aColorme : this.colorme) {
                if (aColorme == null) continue;
                temp = aColorme.original.matcher(temp).replaceFirst(aColorme.replacer);
            }
            result.append(temp);
        }
        return result.toString();
    }

    protected void appendToConsole(String _text) {
        if ((_text = this.fixText(_text)).length() == 0) {
            return;
        }
        if (_text.endsWith("\n") || _text.endsWith("\r")) {
            if (!this.promptLock) {
                this.colors.append(this.console, _text);
                if (this.log != null) {
                    this.log.print(this.colors.strip(_text));
                }
            } else {
                this.colors.append(this.console, this.prompt.getText());
            }
            if (!_text.startsWith(this.prompt.getText())) {
                this.promptLock = false;
            }
        } else {
            int breakp = _text.lastIndexOf("\n");
            if (breakp != -1) {
                this.colors.append(this.console, _text.substring(0, breakp + 1));
                this.updatePrompt(_text.substring(breakp + 1) + " ");
                if (this.log != null) {
                    this.log.print(this.colors.strip(_text.substring(0, breakp + 1)));
                }
            } else {
                this.updatePrompt(_text);
            }
            this.promptLock = true;
        }
        if (this.console.getDocument().getLength() >= 1) {
            this.console.setCaretPosition(this.console.getDocument().getLength() - 1);
        }
    }

    public void append(final String _text) {
        if (_text == null) {
            return;
        }
        CommonUtils.runSafe(() -> Console.this.appendToConsole(_text));
    }

    public void clear() {
        CommonUtils.runSafe(() -> Console.this.console.setText(""));
    }

    public void noInput() {
        CommonUtils.runSafe(() -> {
            Console.this.remove(Console.this.bottom);
            Console.this.validate();
        });
    }

    public Console() {
        this(new Properties(), false);
    }

    public Console(boolean wantsbar) {
        this(new Properties(), wantsbar);
    }

    public Console(Properties display, boolean wantsbar) {
        this.display = display;
        this.consoleFont = Prefs.getPreferences().getFont("console.font.font", "Monospaced BOLD 14");
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(2, 2, 2, 2));
        this.console = new JTextPane();
        this.console.setEditable(false);
        this.console.addFocusListener(this);
        this.console.setCaret(new DefaultCaret() {

            @Override
            public void setSelectionVisible(boolean visible) {
                super.setSelectionVisible(true);
            }
        });
        JScrollPane scroll = new JScrollPane(this.console, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scroll, "Center");
        this.prompt = new JTextPane();
        this.prompt.setEditable(false);
        this.input = new JTextField();
        this.input.setKeymap(JTextField.addKeymap(null, this.input.getKeymap()));
        this.input.addMouseListener(new MouseAdapter() {

            public void checkEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Console.this.getPopupMenu((JTextComponent) e.getSource()).show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                this.checkEvent(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                this.checkEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                this.checkEvent(e);
            }
        });
        this.input.setFocusTraversalKeys(0, new HashSet<>());
        this.input.setFocusTraversalKeys(1, new HashSet<>());
        this.input.setFocusTraversalKeys(2, new HashSet<>());
        this.bottom = new JPanel();
        this.bottom.setLayout(new BorderLayout());
        this.status = new StatusBar(display);
        if (wantsbar) {
            this.bottom.add(this.status, "North");
        }
        this.bottom.add(this.input, "Center");
        this.bottom.add(this.prompt, "West");
        this.add(this.bottom, "South");
        this.components.add(this.input);
        this.components.add(this.console);
        this.components.add(scroll);
        this.components.add(this.prompt);
        this.components.add(this.bottom);
        this.components.add(this.status);
        this.components.add(this);
        this.updateComponentLooks();
        this.addActionForKeySetting("console.clear_screen.shortcut", "ctrl K", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Console.this.console.setText("");
            }
        });
        this.addActionForKeySetting("console.select_all.shortcut", "ctrl A", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Console.this.console.requestFocus();
                Console.this.console.selectAll();
            }
        });
        this.addActionForKeySetting("console.clear_buffer.shortcut", "ESCAPE", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Console.this.input.setText("");
            }
        });
        this.setupFindShortcutFeature();
        this.setupPageShortcutFeature();
        this.setupFontShortcutFeature();
        this.setupHistoryFeature();
        this.clickl = new ClickListener(this);
        this.console.addMouseListener(this.clickl);
        Color background = Prefs.getPreferences().getColor("console.background.color", "#000000");
        this.console.setBackground(new Color(0, 0, 0, 0));
        this.prompt.setBackground(new Color(0, 0, 0, 0));
        scroll.getViewport().setBackground(background);
        this.console.setOpaque(false);
    }

    public StatusBar getStatusBar() {
        return this.status;
    }

    public JPopupMenu getPopupMenu(final JTextComponent _component) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem cut = new JMenuItem("Cut", 67);
        JMenuItem copy2 = new JMenuItem("Copy", 111);
        JMenuItem paste = new JMenuItem("Paste", 80);
        JMenuItem clear = new JMenuItem("Clear", 108);
        if (_component.isEditable()) {
            menu.add(cut);
        }
        menu.add(copy2);
        menu.add(paste);
        menu.add(clear);
        cut.addActionListener(ev -> _component.cut());
        copy2.addActionListener(ev -> _component.copy());
        cut.addActionListener(ev -> _component.cut());
        paste.addActionListener(ev -> Console.this.input.paste());
        clear.addActionListener(ev -> _component.setText(""));
        return menu;
    }

    private void setupFindShortcutFeature() {
        Properties myDisplay = this.display;
        final Console myConsole = this;
        this.addActionForKeySetting("console.find.shortcut", "ctrl pressed F", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Color highlight = Prefs.getPreferences().getColor("console.highlight.color", "#0000cc");
                final SearchPanel search = new SearchPanel(Console.this.console, highlight);
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
                Console.this.changeFontSize(1.0f);
            }
        });
        this.addActionForKeySetting("console.font_size_minus.shortcut", "ctrl MINUS", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Console.this.changeFontSize(-1.0f);
            }
        });
        this.addActionForKeySetting("console.font_size_reset.shortcut", "ctrl pressed 0", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Console.this.consoleFont = Prefs.getPreferences().getFont("console.font.font", "Monospaced BOLD 14");
                Console.this.updateComponentLooks();
            }
        });
    }

    private void setupPageShortcutFeature() {
        this.addActionForKeySetting("console.page_up.shortcut", "pressed PAGE_UP", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Rectangle visible = new Rectangle(Console.this.console.getVisibleRect());
                Rectangle scrollme = new Rectangle(0, (int) (visible.getY() - visible.getHeight() / 2.0), 1, 1);
                if (scrollme.getY() <= 0.0) {
                    visible.setLocation(0, 0);
                }
                Console.this.console.scrollRectToVisible(scrollme);
            }
        });
        this.addActionForKeySetting("console.page_down.shortcut", "pressed PAGE_DOWN", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Rectangle visible = new Rectangle(Console.this.console.getVisibleRect());
                Rectangle scrollme = new Rectangle(0, (int) (visible.getY() + visible.getHeight() + visible.getHeight() / 2.0), 1, 1);
                if (scrollme.getY() >= (double) Console.this.console.getHeight()) {
                    visible.setLocation(0, Console.this.console.getHeight());
                }
                Console.this.console.scrollRectToVisible(scrollme);
            }
        });
    }

    private void setupHistoryFeature() {
        this.input.addActionListener(ev -> {
            if (!"".equals(ev.getActionCommand())) {
                Console.this.history.add(ev.getActionCommand());
            }
        });
        this.addActionForKeySetting("console.history_previous.shortcut", "UP", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                if (Console.this.history.hasPrevious()) {
                    Console.this.input.setText((String) Console.this.history.previous());
                } else {
                    Console.this.input.setText("");
                }
            }
        });
        this.addActionForKeySetting("console.history_next.shortcut", "DOWN", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                if (Console.this.history.hasNext()) {
                    Console.this.input.setText((String) Console.this.history.next());
                } else {
                    Console.this.input.setText("");
                }
            }
        });
    }

    private void changeFontSize(float difference) {
        this.consoleFont = this.consoleFont.deriveFont(this.consoleFont.getSize2D() + difference);
        this.updateComponentLooks();
    }

    public void addActionForKeyStroke(KeyStroke key, Action action) {
        this.input.getKeymap().addActionForKeyStroke(key, action);
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

    @Override
    public void focusGained(FocusEvent ev) {
        if (!ev.isTemporary() && ev.getComponent() == this.console && !(System.getProperty("os.name") + "").contains("Windows") && !(System.getProperty("os.name") + "").contains("Mac")) {
            this.input.requestFocusInWindow();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        return this.input.requestFocusInWindow();
    }

    @Override
    public void focusLost(FocusEvent ev) {
    }

    private static class Replacements {
        public Pattern original;
        public String replacer;

        public Replacements(String o, String r) {
            this.original = Pattern.compile(o);
            this.replacer = r;
        }
    }

    public class ClickListener
            extends MouseAdapter {
        protected LinkedList listeners = new LinkedList();
        protected ConsolePopup popup = null;
        protected Console parent;

        public ClickListener(Console parent) {
            this.parent = parent;
        }

        public void setPopup(ConsolePopup popup) {
            this.popup = popup;
        }

        public void addListener(ActionListener l) {
            this.listeners.add(l);
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
            if (ev.isPopupTrigger()) {
                if (this.popup != null && Console.this.console.getSelectedText() == null) {
                    String result = this.resolveWord(ev.getPoint());
                    this.popup.showPopup(result, ev);
                } else {
                    Console.this.getPopupMenu((JTextComponent) ev.getSource()).show((JComponent) ev.getSource(), ev.getX(), ev.getY());
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent ev) {
            if (!ev.isPopupTrigger()) {
                String result = this.resolveWord(ev.getPoint());
                Iterator i = this.listeners.iterator();
                ActionEvent event = new ActionEvent(this.parent, 0, result);
                if (!"".equals(result)) {
                    while (i.hasNext()) {
                        ActionListener l = (ActionListener) i.next();
                        l.actionPerformed(new ActionEvent(this.parent, 0, result));
                    }
                }
            } else {
                this.checkPopup(ev);
            }
        }

        public String resolveWord(Point pt) {
            int position = Console.this.console.viewToModel(pt);
            String data = Console.this.console.getText().replace("\n", " ").replaceAll("\\s", " ");
            int start = data.lastIndexOf(" ", position);
            int end = data.indexOf(" ", position);
            if (start == -1) {
                start = 0;
            }
            if (end == -1) {
                end = data.length();
            }
            if (end >= start) {
                return data.substring(start, end).trim();
            }
            return null;
        }
    }

}

