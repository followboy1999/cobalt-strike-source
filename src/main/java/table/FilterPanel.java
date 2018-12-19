package table;

import common.CommonUtils;
import filter.DataFilter;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class FilterPanel
        extends JPanel implements ActionListener {
    protected JTextField filter;
    protected JLabel status;
    protected ATable table;
    protected JComboBox cols;
    protected StringBuilder desc = new StringBuilder();
    protected JToggleButton negate = new JToggleButton(" ! ");
    protected DataFilter action = new DataFilter();

    public String getColumn() {
        return this.cols.getSelectedItem().toString();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if ("".equals(this.filter.getText())) {
            return;
        }
        if (CommonUtils.contains("internal, external, host, address, fhost", this.getColumn())) {
            this.action.checkNetwork(this.getColumn(), this.filter.getText(), this.negate.isSelected());
        } else if (CommonUtils.contains("rx, tx, port, fport, Size, size, pid, last", this.getColumn())) {
            this.action.checkNumber(this.getColumn(), this.filter.getText(), this.negate.isSelected());
        } else {
            this.action.checkWildcard(this.getColumn(), "*" + this.filter.getText() + "*", this.negate.isSelected());
        }
        ((GenericTableModel) this.table.getModel()).apply(this.action);
        this.filter.setText("");
        this.negate.setSelected(false);
        this.status.setText(this.action.toString() + " applied.");
    }

    @Override
    public void requestFocus() {
        this.filter.requestFocus();
    }

    public void clear() {
        this.status.setText("");
        this.desc = new StringBuilder();
        ((GenericTableModel) this.table.getModel()).reset();
    }

    public FilterPanel(ATable table) {
        this.table = table;
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(1, 1, 1, 1));
        List temp = CommonUtils.toList(((GenericTableModel) table.getModel()).getColumnNames());
        temp.remove(" ");
        temp.remove("D");
        temp.remove("date");
        temp.remove("Modified");
        this.cols = new JComboBox<>(CommonUtils.toArray(temp));
        this.filter = new JTextField(15);
        this.filter.addActionListener(this);
        JButton reset2 = new JButton("Reset");
        reset2.addActionListener(ev -> {
            FilterPanel.this.action.reset();
            FilterPanel.this.clear();
        });
        JPanel holder = new JPanel();
        holder.setLayout(new FlowLayout());
        holder.add(new JLabel("Filter: "));
        holder.add(this.negate);
        holder.add(this.filter);
        holder.add(this.cols);
        this.add(holder, "West");
        JPanel east = new JPanel();
        east.setLayout(new FlowLayout());
        east.add(reset2);
        this.add(east, "East");
        this.status = new JLabel("");
        this.add(this.status, "Center");
        this.negate.setToolTipText("Negate this filter.");
    }

}

