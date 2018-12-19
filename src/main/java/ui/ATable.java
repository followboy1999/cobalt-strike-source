package ui;

import common.CommonUtils;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EventObject;
import java.util.HashSet;

public class ATable
        extends JTable {
    public static final String indicator = " \u25aa";
    protected boolean alternateBackground = false;
    protected TableClickListener clickl = new TableClickListener();
    protected int[] selected = null;

    public void markSelections() {
        this.selected = this.getSelectedRows();
    }

    public void setPopupMenu(TablePopup menu) {
        this.clickl.setPopup(menu);
    }

    public void fixSelection() {
        if (this.selected.length == 0) {
            return;
        }
        this.getSelectionModel().setValueIsAdjusting(true);
        int rowcount = this.getModel().getRowCount();
        for (int aSelected : this.selected) {
            if (aSelected >= rowcount) continue;
            this.getSelectionModel().addSelectionInterval(aSelected, aSelected);
        }
        this.getSelectionModel().setValueIsAdjusting(false);
    }

    public void restoreSelections() {
        CommonUtils.runSafe(ATable.this::fixSelection);
    }

    public static TableCellRenderer getDefaultTableRenderer(JTable table, final TableModel model) {
        final HashSet<String> specialitems = new HashSet<>();
        specialitems.add("Wordlist");
        specialitems.add("PAYLOAD");
        specialitems.add("RHOST");
        specialitems.add("RHOSTS");
        specialitems.add("Template");
        specialitems.add("DICTIONARY");
        specialitems.add("NAMELIST");
        specialitems.add("SigningKey");
        specialitems.add("SigningCert");
        specialitems.add("WORDLIST");
        specialitems.add("SESSION");
        specialitems.add("REXE");
        specialitems.add("EXE::Custom");
        specialitems.add("EXE::Template");
        specialitems.add("USERNAME");
        specialitems.add("PASSWORD");
        specialitems.add("SMBUser");
        specialitems.add("SMBPass");
        specialitems.add("INTERFACE");
        specialitems.add("URL");
        specialitems.add("PATH");
        specialitems.add("SCRIPT");
        specialitems.add("KEY_PATH");
        return (table1, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table1.getDefaultRenderer(String.class);
            String content = (value != null ? value : "") + "";
            if (specialitems.contains(content) || content.contains("FILE")) {
                content = content + ATable.indicator;
            }
            JComponent c = (JComponent) render.getTableCellRendererComponent(table1, content, isSelected, false, row, column);
            c.setToolTipText(((GenericTableModel) model).getValueAtColumn(table1, row, "Tooltip") + "");
            return c;
        };
    }

    public static TableCellRenderer getFileTypeTableRenderer() {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JComponent c = (JComponent) render.getTableCellRendererComponent(table, "", isSelected, false, row, column);
            if ("dir".equals(value)) {
                ((JLabel) c).setIcon(UIManager.getIcon("FileView.directoryIcon"));
            } else if ("drive".equals(value)) {
                ((JLabel) c).setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
            } else {
                ((JLabel) c).setIcon(UIManager.getIcon("FileView.fileIcon"));
            }
            return c;
        };
    }

    public static TableCellRenderer getListenerStatusRenderer(final GenericTableModel model) {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JLabel component = (JLabel) render.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            Object valuez = model.getValueAt(table, row, "status");
            if (valuez != null && !"".equals(valuez) && !"success".equals(valuez)) {
                component.setText("<html><body>" + component.getText() + " <font color=\"#8b0000\"><strong>ERROR!</strong> " + valuez + "</font></body></html>");
            }
            return component;
        };
    }

    public static TableCellRenderer getBoldOnKeyRenderer(final GenericTableModel model, final String key) {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JLabel component = (JLabel) render.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            if (model.getValueAt(table, row, key) == Boolean.TRUE) {
                component.setFont(component.getFont().deriveFont(Font.BOLD));
            } else {
                component.setFont(component.getFont().deriveFont(Font.PLAIN));
            }
            return component;
        };
    }

    public static TableCellRenderer getSimpleTableRenderer() {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JComponent c = (JComponent) render.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            ((JLabel) c).setIcon(null);
            return c;
        };
    }

    public static TableCellRenderer getSizeTableRenderer() {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JComponent c = (JComponent) render.getTableCellRendererComponent(table, "", isSelected, false, row, column);
            try {
                long size = Long.parseLong(value + "");
                String units = "b";
                if (size > 1024L) {
                    size /= 1024L;
                    units = "kb";
                }
                if (size > 1024L) {
                    size /= 1024L;
                    units = "mb";
                }
                if (size > 1024L) {
                    size /= 1024L;
                    units = "gb";
                }
                ((JLabel) c).setText(size + units);
            } catch (Exception ex) {
                // empty catch block
            }
            return c;
        };
    }

    public static TableCellRenderer getTimeTableRenderer() {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JComponent c = (JComponent) render.getTableCellRendererComponent(table, "", isSelected, false, row, column);
            try {
                long size = Long.parseLong(value + "");
                String units = "ms";
                if (size <= 1000L) {
                    ((JLabel) c).setText(size + units);
                    return c;
                }
                units = "s";
                if ((size /= 1000L) > 60L) {
                    size /= 60L;
                    units = "m";
                }
                if (size > 60L) {
                    size /= 60L;
                    units = "h";
                }
                ((JLabel) c).setText(size + units);
            } catch (Exception ex) {
                // empty catch block
            }
            return c;
        };
    }

    public static TableCellRenderer getImageTableRenderer(final GenericTableModel model, final String icol) {
        return (table, value, isSelected, hasFocus, row, col) -> {
            JLabel component = (JLabel) table.getDefaultRenderer(Object.class).getTableCellRendererComponent(table, value, isSelected, false, row, col);
            ImageIcon image = (ImageIcon) model.getValueAt(table, row, icol);
            if (image != null) {
                component.setIcon(image);
                component.setText("");
            } else {
                component.setIcon(null);
                component.setText("");
            }
            return component;
        };
    }

    public static TableCellRenderer getDateTableRenderer() {
        return (table, value, isSelected, hasFocus, row, column) -> {
            TableCellRenderer render = table.getDefaultRenderer(String.class);
            JComponent c = (JComponent) render.getTableCellRendererComponent(table, "", isSelected, false, row, column);
            try {
                long size = Long.parseLong(value + "");
                ((JLabel) c).setText(CommonUtils.formatDate(size));
            } catch (Exception ex) {
                // empty catch block
            }
            return c;
        };
    }

    public void adjust() {
        this.addMouseListener(this.clickl);
        this.setShowGrid(false);
        this.setIntercellSpacing(new Dimension(0, 0));
        this.setRowHeight(this.getRowHeight() + 2);
        final TableCellEditor defaulte = this.getDefaultEditor(Object.class);
        this.setDefaultEditor(Object.class, new TableCellEditor() {

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int col) {
                Component editor = defaulte.getTableCellEditorComponent(table, value, selected, row, col);
                if (editor instanceof JTextComponent) {
                    new CutCopyPastePopup((JTextComponent) editor);
                }
                return editor;
            }

            @Override
            public void addCellEditorListener(CellEditorListener l) {
                defaulte.addCellEditorListener(l);
            }

            @Override
            public void cancelCellEditing() {
                defaulte.cancelCellEditing();
            }

            @Override
            public Object getCellEditorValue() {
                return defaulte.getCellEditorValue();
            }

            @Override
            public boolean isCellEditable(EventObject anEvent) {
                return defaulte.isCellEditable(anEvent);
            }

            @Override
            public void removeCellEditorListener(CellEditorListener l) {
                defaulte.removeCellEditorListener(l);
            }

            @Override
            public boolean shouldSelectCell(EventObject anEvent) {
                return defaulte.shouldSelectCell(anEvent);
            }

            @Override
            public boolean stopCellEditing() {
                return defaulte.stopCellEditing();
            }
        });
        final TableCellRenderer defaultr = this.getDefaultRenderer(Object.class);
        this.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) -> {
            if (value == null) {
                value = "";
            }
            return defaultr.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        });
    }

    public ATable() {
        this.adjust();
    }

    public ATable(TableModel model) {
        super(model);
        this.adjust();
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        this.alternateBackground = row % 2 == 0;
        Component component = super.prepareRenderer(renderer, row, column);
        if (!Color.WHITE.equals(component.getForeground())) {
            ((JComponent) component).setOpaque(true);
            component.setBackground(this.getComponentBackground());
        }
        return component;
    }

    public Color getComponentBackground() {
        return this.alternateBackground ? new Color(16250873) : Color.WHITE;
    }

    public void addActionForKeyStroke(KeyStroke key, Action action) {
        this.getActionMap().put(key.toString(), action);
        this.getInputMap().put(key, key.toString());
    }

    public void addActionForKey(String key, Action action) {
        this.addActionForKeyStroke(KeyStroke.getKeyStroke(key), action);
    }

    public BufferedImage getScreenshot() {
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), 6);
        Graphics g = image.getGraphics();
        this.paint(g);
        g.dispose();
        return image;
    }

}

