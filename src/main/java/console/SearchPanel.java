package console;

import dialog.DialogUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchPanel
        extends JPanel implements ActionListener {
    protected JTextField search;
    protected JLabel status;
    protected JTextComponent component;
    protected int index = 0;
    protected Color highlight;

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (event.getActionCommand()) {
            case ">":
                ++this.index;
                this.scrollToIndex();
                break;
            case "<":
                --this.index;
                this.scrollToIndex();
                break;
            default:
                this.searchBuffer();
                this.scrollToIndex();
                break;
        }
    }

    private void scrollToIndex() {
        Highlighter.Highlight[] highlights = this.component.getHighlighter().getHighlights();
        if (highlights.length == 0) {
            if (this.search.getText().trim().length() > 0) {
                this.status.setText("Phrase not found");
            }
            return;
        }
        try {
            if (this.index < 0) {
                this.index = highlights.length - 1 - this.index;
            }
            int offset = this.index % highlights.length;
            this.status.setText(offset + 1 + " of " + highlights.length);
            int position = highlights[offset].getStartOffset();
            Rectangle location = this.component.modelToView(position);
            this.component.scrollRectToVisible(location);
        } catch (BadLocationException ex) {
            // empty catch block
        }
    }

    private void searchBuffer() {
        this.clear();
        String searchstr = this.search.getText().trim();
        if (searchstr.length() == 0) {
            return;
        }
        DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(this.highlight);
        try {
            String text = this.component.getText();
            if ((System.getProperty("os.name") + "").contains("Windows")) {
                text = text.replaceAll("\r\n", "\n");
            }
            int lastIndex = -1;
            while ((lastIndex = text.indexOf(searchstr, lastIndex + 1)) != -1) {
                this.component.getHighlighter().addHighlight(lastIndex, lastIndex + searchstr.length(), painter);
            }
        } catch (Exception ex) {
            // empty catch block
        }
    }

    @Override
    public void requestFocus() {
        this.search.requestFocus();
    }

    public void clear() {
        this.component.getHighlighter().removeAllHighlights();
        this.index = 0;
        this.status.setText("");
    }

    public SearchPanel(JTextComponent component, Color highlight) {
        this.component = component;
        this.highlight = highlight;
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(1, 1, 1, 1));
        JButton previous = new JButton("<");
        previous.setActionCommand("<");
        JButton next = new JButton(">");
        next.setActionCommand(">");
        DialogUtils.removeBorderFromButton(previous);
        DialogUtils.removeBorderFromButton(next);
        previous.addActionListener(this);
        next.addActionListener(this);
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(1, 2));
        buttons.add(previous);
        buttons.add(next);
        this.search = new JTextField(15);
        this.search.addActionListener(this);
        JPanel holder = new JPanel();
        holder.setLayout(new FlowLayout());
        holder.add(new JLabel("Find: "));
        holder.add(this.search);
        holder.add(buttons);
        this.add(holder, "West");
        this.status = new JLabel("");
        this.add(this.status, "Center");
    }
}

