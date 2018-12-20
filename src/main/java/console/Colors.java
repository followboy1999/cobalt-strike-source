package console;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.Properties;

public class Colors {
    public static final char bold = '\u0002';
    public static final char underline = '\u001f';
    public static final char color = '\u0003';
    public static final char cancel = '\u000f';
    public static final char reverse = '\u0016';
    protected boolean showcolors;
    protected Color[] colorTable = new Color[16];
    private StyledDocument dummy = new DefaultStyledDocument();
    private static final int MAX_DOCUMENT_LENGTH = 262144;

    public static String color(String text, String choice) {
        return '\u0003' + choice + text;
    }

    public static String underline(String text) {
        return underline + text + cancel;
    }

    public Colors(Properties prefs) {
        this.colorTable[0] = Color.white;
        this.colorTable[1] = new Color(0, 0, 0);
        this.colorTable[2] = Color.decode("#3465A4");
        this.colorTable[3] = Color.decode("#4E9A06");
        this.colorTable[4] = Color.decode("#EF2929");
        this.colorTable[5] = Color.decode("#CC0000");
        this.colorTable[6] = Color.decode("#75507B");
        this.colorTable[7] = Color.decode("#C4A000");
        this.colorTable[8] = Color.decode("#FCE94F");
        this.colorTable[9] = Color.decode("#8AE234");
        this.colorTable[10] = Color.decode("#06989A");
        this.colorTable[11] = Color.decode("#34E2E2");
        this.colorTable[12] = Color.decode("#729FCF");
        this.colorTable[13] = Color.decode("#AD7FA8");
        this.colorTable[14] = Color.decode("#808080");
        this.colorTable[15] = Color.lightGray;
        for (int x = 0; x < 16; ++x) {
            String temps = prefs.getProperty("console.color_" + x + ".color", null);
            if (temps == null) continue;
            this.colorTable[x] = Color.decode(temps);
        }
        this.showcolors = "true".equals(prefs.getProperty("console.show_colors.boolean", "true"));
    }

    public String strip(String text) {
        Fragment f = this.parse(text);
        return this.strip(f);
    }

    private String strip(Fragment f) {
        StringBuilder buffer = new StringBuilder(128);
        while (f != null) {
            buffer.append(f.text);
            f = f.next;
        }
        return buffer.toString();
    }

    private void append(StyledDocument doc, Fragment f) {
        while (f != null) {
            try {
                if (f.text.length() > 0) {
                    doc.insertString(doc.getLength(), f.text.toString(), f.attr);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            f = f.next;
        }
    }

    public void append(StyledDocument doc, String text) {
        if (text.length() > 262144) {
            text = text.substring(text.length() - 262144);
        }
        Fragment f = this.parse(text);
        this.append(doc, f);
        if (doc.getLength() > 262144) {
            try {
                doc.remove(0, doc.getLength() - 262144 + 131072);
            } catch (BadLocationException ble) {
                // empty catch block
            }
        }
    }

    public void append(JTextPane console, String text) {
        StyledDocument doc = console.getStyledDocument();
        if (this.showcolors) {
            console.setDocument(this.dummy);
            this.append(doc, text);
            console.setDocument(doc);
        } else {
            Fragment f = this.parse(text);
            this.append(doc, this.parse(this.strip(f)));
        }
    }

    public void set(JTextPane console, String text) {
        Fragment f = this.parse(text);
        if (this.strip(f).equals(console.getText())) {
            return;
        }
        DefaultStyledDocument doc = new DefaultStyledDocument();
        if (this.showcolors) {
            this.append(doc, f);
        } else {
            this.append(doc, this.parse(this.strip(f)));
        }
        console.setDocument(doc);
        console.setSize(new Dimension(1000, console.getSize().height));
    }

    public void setNoHack(JTextPane console, String text) {
        Fragment f = this.parse(text);
        if (this.strip(f).equals(console.getText())) {
            return;
        }
        DefaultStyledDocument doc = new DefaultStyledDocument();
        if (this.showcolors) {
            this.append(doc, f);
        } else {
            this.append(doc, this.parse(this.strip(f)));
        }
        console.setDocument(doc);
    }

    private Fragment parse(String text) {
        Fragment current;
        Fragment first = current = new Fragment();
        if (text == null) {
            return current;
        }
        char[] data = text.toCharArray();
        block7:
        for (int x = 0; x < data.length; ++x) {
            switch (data[x]) {
                case '\u0002': {
                    current.advance();
                    StyleConstants.setBold(current.next.attr, !StyleConstants.isBold(current.attr));
                    current = current.next;
                    continue block7;
                }
                case '\u001f': {
                    current.advance();
                    StyleConstants.setUnderline(current.next.attr, !StyleConstants.isUnderline(current.attr));
                    current = current.next;
                    continue block7;
                }
                case '\u0003': {
                    current.advance();
                    if (x + 1 < data.length && (data[x + 1] >= '0' && data[x + 1] <= '9' || data[x + 1] >= 'A' && data[x + 1] <= 'F')) {
                        int index = Integer.parseInt(data[x + 1] + "", 16);
                        StyleConstants.setForeground(current.next.attr, this.colorTable[index]);
                        ++x;
                    }
                    current = current.next;
                    continue block7;
                }
                case '\n': {
                    current.advance();
                    current = current.next;
                    current.attr = new SimpleAttributeSet();
                    current.text.append(data[x]);
                    continue block7;
                }
                case '\u000f': {
                    current.advance();
                    current = current.next;
                    current.attr = new SimpleAttributeSet();
                    continue block7;
                }
                default: {
                    current.text.append(data[x]);
                }
            }
        }
        return first;
    }

    private static final class Fragment {
        protected SimpleAttributeSet attr = new SimpleAttributeSet();
        protected StringBuilder text = new StringBuilder(32);
        protected Fragment next = null;

        private Fragment() {
        }

        public void advance() {
            this.next = new Fragment();
            this.next.attr = (SimpleAttributeSet) this.attr.clone();
        }
    }

}

