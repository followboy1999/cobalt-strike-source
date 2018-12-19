package ui;

import common.CommonUtils;
import filter.DataFilter;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GenericTableModel
        extends AbstractTableModel {
    protected String[] columnNames;
    protected ArrayList<Map<String, Object>> rows;
    protected String leadColumn;
    protected boolean[] editable;
    protected ArrayList<Map<String, Object>> all;
    protected DataFilter filter = null;

    public void apply(DataFilter filter) {
        synchronized (this) {
            ArrayList<Map<String, Object>> temp = new ArrayList<Map<String, Object>>(filter.apply(this.rows));
            this.rows = new ArrayList<>(temp.size());
            this.rows.addAll(temp);
            this.filter = filter;
        }
        this.fireListeners();
    }

    public void reset() {
        synchronized (this) {
            this.rows = new ArrayList<>(this.all.size());
            this.rows.addAll(this.all);
            this.filter = null;
        }
        this.fireListeners();
    }

    public List<Map<String, Object>> getRows() {
        return this.rows;
    }

    public List export() {
        synchronized (this) {
            return this.rows.stream().map(HashMap::new).collect(Collectors.toCollection(LinkedList::new));
        }
    }

    public GenericTableModel(String[] columnNames, String leadColumn, int anticipatedSize) {
        this.columnNames = columnNames;
        this.leadColumn = leadColumn;
        this.rows = new ArrayList<>(anticipatedSize);
        this.all = new ArrayList<>(anticipatedSize);
        this.editable = new boolean[columnNames.length];
        for (int x = 0; x < this.editable.length; ++x) {
            this.editable[x] = false;
        }
    }

    public void setCellEditable(int column) {
        this.editable[column] = true;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return this.editable[column];
    }

    public Object[] getSelectedValues(JTable t) {
        synchronized (this) {
            int[] row = t.getSelectedRows();
            Object[] rv = new Object[row.length];
            IntStream.range(0, row.length).forEach(x -> {
                int r = t.convertRowIndexToModel(row[x]);
                rv[x] = r < this.rows.size() && r >= 0 ? this.rows.get(r).get(this.leadColumn) : null;
            });
            return rv;
        }
    }

    public Map[] getSelectedRows(JTable t) {
        synchronized (this) {
            int[] row = t.getSelectedRows();
            Map[] rv = new HashMap[row.length];
            for (int x = 0; x < row.length; ++x) {
                int r = t.convertRowIndexToModel(row[x]);
                rv[x] = this.rows.get(r);
            }
            return rv;
        }
    }

    public Object[][] getSelectedValuesFromColumns(JTable t, String[] cols) {
        synchronized (this) {
            int[] row = t.getSelectedRows();
            Object[][] rv = new Object[row.length][cols.length];
            for (int x = 0; x < row.length; ++x) {
                int r = t.convertRowIndexToModel(row[x]);
                for (int y = 0; y < cols.length; ++y) {
                    rv[x][y] = this.rows.get(r).get(cols[y]);
                }
            }
            return rv;
        }
    }

    public Object getSelectedValue(JTable t) {
        synchronized (this) {
            Object[] values = this.getSelectedValues(t);
            if (values.length == 0) {
                return null;
            }
            return values[0];
        }
    }

    public Object getValueAt(JTable t, int row, String column) {
        synchronized (this) {
            row = t.convertRowIndexToModel(row);
            if (row == -1) {
                return null;
            }
            return this.rows.get(row).get(column);
        }
    }

    public int getSelectedRow(JTable t) {
        synchronized (this) {
            return t.convertRowIndexToModel(t.getSelectedRow());
        }
    }

    public void _setValueAtRow(int row, String column, String value) {
        this.rows.get(row).put(column, value);
    }

    public void setValueForKey(String key, String column, String value) {
        int row = -1;
        synchronized (this) {
            Iterator<Map<String, Object>> i = this.rows.iterator();
            int x = 0;
            while (i.hasNext()) {
                Map<String, Object> temp = i.next();
                if (key.equals(temp.get(this.leadColumn))) {
                    row = x;
                    break;
                }
                ++x;
            }
        }
        if (row != -1) {
            this.setValueAtRow(row, column, value);
        }
    }

    public void setValueAtRow(final int row, final String column, final String value) {
        CommonUtils.runSafe(() -> GenericTableModel.this._setValueAtRow(row, column, value));
    }

    public Object getSelectedValueFromColumn(JTable t, String column) {
        synchronized (this) {
            int row = t.getSelectedRow();
            if (row == -1) {
                return null;
            }
            return this.getValueAt(t, row, column);
        }
    }

    @Override
    public String getColumnName(int x) {
        return this.columnNames[x];
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    public String[] getColumnNames() {
        return this.columnNames;
    }

    public void addEntry(Map<String,Object> row) {
        CommonUtils.runSafe(() -> GenericTableModel.this._addEntry(row));
    }

    public void clear(final int newSize) {
        CommonUtils.runSafe(() -> GenericTableModel.this._clear(newSize));
    }

    public void fireListeners() {
        CommonUtils.runSafe(GenericTableModel.this::fireTableDataChanged);
    }

    public void _addEntry(Map<String,Object> row) {
        synchronized (this) {
            if (this.filter == null || this.filter.test(row)) {
                this.rows.add(row);
            }
            this.all.add(row);
            int size = this.rows.size() - 1;
        }
    }

    public void _clear(int anticipatedSize) {
        synchronized (this) {
            this.rows = new ArrayList<>(anticipatedSize);
            this.all = new ArrayList<>(anticipatedSize);
        }
    }

    @Override
    public int getRowCount() {
        synchronized (this) {
            return this.rows.size();
        }
    }

    public Object getValueAtColumn(JTable t, int row, String col) {
        synchronized (this) {
            row = t.convertRowIndexToModel(row);
            Map<String, Object> temp = this.rows.get(row);
            return temp.get(col);
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        synchronized (this) {
            if (row < this.rows.size()) {
                Map<String, Object> temp = this.rows.get(row);
                return temp.get(this.getColumnName(col));
            }
            return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        synchronized (this) {
            Map<String,Object> temp = this.rows.get(row);
            temp.put(this.getColumnName(col), value);
        }
    }

}

