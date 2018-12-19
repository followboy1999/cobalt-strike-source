package table;

import dialog.DialogUtils;
import ui.ATable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class FilterAndScroll
        extends JPanel {
    protected ATable table;

    public FilterAndScroll(ATable table) {
        this.table = table;
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(table), "Center");
        this.setupFindShortcutFeature();
    }

    private void setupFindShortcutFeature() {
        final FilterAndScroll myPanel = this;
        this.table.addActionForKey("ctrl pressed F", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                final FilterPanel filter = new FilterPanel(FilterAndScroll.this.table);
                final JPanel north = new JPanel();
                JButton goaway = new JButton("X ");
                DialogUtils.removeBorderFromButton(goaway);
                goaway.addActionListener(ev1 -> {
                    filter.clear();
                    myPanel.remove(north);
                    myPanel.validate();
                });
                north.setLayout(new BorderLayout());
                north.add(filter, "Center");
                north.add(goaway, "East");
                myPanel.add(north, "South");
                myPanel.validate();
                filter.requestFocusInWindow();
                filter.requestFocus();
            }

        });
    }

}

