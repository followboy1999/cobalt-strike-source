package ui;

import aggressor.ui.UseSynthetica;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Navigator
        extends JComponent implements ListSelectionListener {
    protected CardLayout options = new CardLayout();
    protected JList navigator = new JList();
    protected JPanel switcher = new JPanel();
    protected Map icons = new HashMap();

    public Navigator() {
        this.switcher.setLayout(this.options);
        this.navigator.setFixedCellWidth(125);
        this.setLayout(new BorderLayout());
        this.add(DialogUtils.wrapComponent(new JScrollPane(this.navigator), 5), "West");
        this.add(DialogUtils.wrapComponent(this.switcher, 5), "Center");
        this.navigator.setCellRenderer(new CellRenderer());
        this.navigator.addListSelectionListener(this);
        this.navigator.setModel(new DefaultListModel());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        this.options.show(this.switcher, (String) this.navigator.getSelectedValue());
    }

    public void set(String value) {
        this.navigator.setSelectedValue(value, true);
        this.options.show(this.switcher, value);
    }

    public void addPage(String title, Icon icon, String description, JComponent item) {
        JPanel c = new JPanel();
        c.setLayout(new BorderLayout());
        c.add(DialogUtils.description(description), "North");
        c.add(DialogUtils.top(item), "Center");
        this.icons.put(title, icon);
        DefaultListModel model = (DefaultListModel) this.navigator.getModel();
        model.addElement(title);
        this.switcher.add(c, title);
    }

    public static void main(String[] args) {
        new UseSynthetica().setup();
        JFrame dialog = DialogUtils.dialog("Hello World", 640, 480);
        Navigator nav = new Navigator();
        DialogManager controller = new DialogManager(dialog);
        controller.startGroup("console");
        controller.text("user", "User:", 20);
        controller.text("pass", "Password:", 20);
        controller.text("host", "Host:", 20);
        controller.text("port", "Port:", 10);
        controller.endGroup();
        nav.addPage("Console", new ImageIcon("./resources/cc/black/png/monitor_icon&16.png"), "This is your opportunity to edit console preferences", controller.layout("console"));
        controller.startGroup("console2");
        controller.text("user", "User A:", 20);
        controller.text("pass", "Password:", 20);
        controller.text("host", "Host:", 20);
        controller.text("port", "Port:", 10);
        controller.text("port", "Port:", 10);
        controller.text("port", "Port:", 10);
        controller.endGroup();
        nav.addPage("Console II", new ImageIcon("./resources/cc/black/png/monitor_icon&16.png"), "This is another opportunity to edit stuff. I think you know the drill by now.", controller.layout("console2"));
        dialog.add(nav, "Center");
        dialog.add(DialogUtils.center(controller.action("Close")), "South");
        dialog.setVisible(true);
    }

    private class CellRenderer
            extends JLabel implements ListCellRenderer {
        private CellRenderer() {
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = value.toString();
            this.setText(s);
            this.setIcon((Icon) Navigator.this.icons.get(value));
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);
            return this;
        }
    }

}

