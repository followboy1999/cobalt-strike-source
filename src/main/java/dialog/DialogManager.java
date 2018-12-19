package dialog;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.dialogs.*;
import common.*;
import ui.APasswordField;
import ui.ATable;
import ui.ATextField;
import ui.GenericTableModel;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DialogManager {
    protected HashMap<String, Object> options = new HashMap<String, Object>();
    protected LinkedList<DialogListener> listeners = new LinkedList<>();
    protected LinkedList<DialogListener> listeners2 = new LinkedList<>();
    protected LinkedList<DialogRow> rows = new LinkedList<>();
    protected JFrame dialog;
    protected LinkedList<DialogRow> group = null;
    protected HashMap<String, LinkedList<DialogRow>> groups = new HashMap<>();

    public void addDialogListener(DialogListener d) {
        this.listeners2.add(d);
    }

    public void addDialogListenerInternal(DialogListener d) {
        this.listeners.add(d);
    }

    public LinkedList<DialogRow> getRows() {
        return this.rows;
    }

    public void startGroup(String name) {
        this.group = new LinkedList<>();
        this.groups.put(name, this.group);
    }

    public void endGroup() {
        this.group = null;
    }

    public DialogManager(JFrame d) {
        this.dialog = d;
    }

    public void set(String key, String value) {
        this.options.put(key, value);
    }

    public void set(Map alt) {

        for (Entry next : (Iterable<Entry>) alt.entrySet()) {
            this.options.put(next.getKey() + "", next.getValue() + "");
        }

    }

    private static void setEnabledSafe(final JComponent c, final boolean state) {
        CommonUtils.runSafe(() -> c.setEnabled(state));
    }

    public JButton action_noclose(final String text) {
        JButton button = new JButton(text);
        button.addActionListener(ev -> {
            ((JComponent) ev.getSource()).setEnabled(false);
            (new Thread(() -> {
                Iterator i = DialogManager.this.listeners.iterator();

                DialogListener d;
                while (i.hasNext()) {
                    d = (DialogListener) i.next();
                    d.dialogAction(ev, DialogManager.this.options);
                }

                i = DialogManager.this.listeners2.iterator();

                while (i.hasNext()) {
                    d = (DialogListener) i.next();
                    d.dialogAction(ev, DialogManager.this.options);
                }

                DialogManager.setEnabledSafe((JComponent) ev.getSource(), true);
            }, "dialog action: " + text)).start();
        });
        return button;
    }

    public DialogRow listener_stages(String key, String text, AggressorClient client) {
        List listeners = Listener.getListenerNamesForStagelessPayloads(client.getData());
        return this.combobox(key, text, CommonUtils.toArray(listeners));
    }

    public DialogRow listenerWithSMB(String key, String text, TeamQueue conn, DataManager data) {
        DialogRow row = this.combobox(key, text, CommonUtils.toArray(Listener.getListenerNamesWithSMB(data)));
        JButton add = new JButton("Add");
        add.addActionListener(new DialogManager.ListenerAdd((JComboBox<String>) row.c[1], conn, data));
        row.last(add);
        return row;
    }

    public DialogRow listener(String key, String text, TeamQueue conn, DataManager data) {
        DialogRow row = this.combobox(key, text, CommonUtils.toArray(Listener.getListenerNamesNoSMB()));
        JButton add = new JButton("Add");
        add.addActionListener(new DialogManager.ListenerAdd((JComboBox<String>) row.c[1], conn, data));
        row.last(add);
        return row;
    }

    public DialogRow beacon(final String key, String text, final AggressorClient client) {
        final DialogRow result = this.text(key + ".title", text);
        ((JTextField) result.c[1]).setEditable(false);
        JButton button = new JButton("...");
        button.addActionListener(ev -> (new BeaconChooser(client, r -> {
            DialogManager.this.options.put(key, r);
            BeaconEntry entry = DataUtils.getBeacon(client.getData(), r);
            if (entry != null) {
                ((JTextField) result.c[1]).setText(entry.getUser() + entry.title(" via "));
            }

        })).show());
        result.c[2] = button;
        return result;
    }

    public DialogRow interfaces(String key, String text, TeamQueue conn, DataManager data) {
        List interfaces = DataUtils.getInterfaceList(data);
        DialogRow row = this.combobox(key, text, CommonUtils.toArray(interfaces));
        JButton add = new JButton("Add");
        add.addActionListener(new DialogManager.InterfaceAdd((JComboBox<String>) row.c[1], conn, data));
        row.last(add);
        return row;
    }

    public DialogRow exploits(String key, String text, AggressorClient client) {
        List exploits = DataUtils.getBeaconExploits(client.getData()).exploits();
        return this.combobox(key, text, CommonUtils.toArray(exploits));
    }

    public DialogRow krbtgt(String key, String text, final AggressorClient client) {
        DialogRow result = this.text(key, text);
        final JTextField textc = (JTextField) result.c[1];
        JButton button = new JButton("...");
        button.addActionListener(ev -> {
            CredentialChooser chooser = new CredentialChooser(client, r -> {
                String[] temp = r.split(" ");
                textc.setText(temp[1]);
            });
            chooser.getFilter().checkLiteral("user", "krbtgt");
            chooser.getFilter().checkNTLMHash("password", false);
            chooser.show();
        });
        result.c[2] = button;
        return result;
    }

    public DialogRow label(String text) {
        DialogRow row = new DialogRow(new JPanel(), new JLabel(text), new JPanel());
        this.rows.add(row);
        if (this.group != null) {
            this.group.add(row);
        }

        return row;
    }

    public DialogRow combobox(final String key, String text, String[] items) {
        new JLabel(text);
        final JComboBox<String> combobox = new JComboBox<>(items);
        combobox.setPreferredSize(new Dimension(240, 0));
        if (this.options.containsKey(key)) {
            combobox.setSelectedItem(this.options.get(key));
        }

        this.addDialogListenerInternal((event, options) -> options.put(key, combobox.getSelectedItem()));
        DialogRow result = new DialogRow(new JLabel(text), combobox, new JPanel());
        this.rows.add(result);
        if (this.group != null) {
            this.group.add(result);
        }

        return result;
    }

    public DialogRow attack(String key, String text) {
        DialogRow result = this.text(key, text);
        JTextField textc = (JTextField) result.c[1];
        JButton button = new JButton("...");
        button.addActionListener(ev -> {
        });
        result.c[2] = button;
        return result;
    }

    public DialogRow site(String key, String text, final TeamQueue conn, final DataManager data) {
        DialogRow result = this.text(key, text);
        final JTextField textc = (JTextField) result.c[1];
        JButton button = new JButton("...");
        button.addActionListener(ev -> (new SiteChooser(conn, data, r -> textc.setText(r + "?id=%TOKEN%"))).show());
        result.c[2] = button;
        return result;
    }

    public DialogRow proxyserver(String key, String text, final AggressorClient client) {
        DialogRow result = this.text(key, text);
        final JTextField textc = (JTextField) result.c[1];
        JButton button = new JButton("...");
        button.addActionListener(ev -> (new ProxyServerDialog(textc.getText(), r -> {
            client.getConnection().call("armitage.broadcast", CommonUtils.args("manproxy", r));
            textc.setText(r);
        })).show());
        result.c[2] = button;
        return result;
    }

    public DialogRow mailserver(String key, String text) {
        DialogRow result = this.text(key, text);
        final JTextField textc = (JTextField) result.c[1];
        JButton button = new JButton("...");
        button.addActionListener(ev -> (new MailServerDialog(textc.getText(), textc::setText)).show());
        result.c[2] = button;
        return result;
    }

    public DialogRow file(String key, String text) {
        DialogRow result = this.text(key, text);
        final JTextField textc = (JTextField) result.c[1];
        JButton button = new JButton(FileSystemView.getFileSystemView().getSystemIcon(new File(".")));
        button.addActionListener(ev -> SafeDialogs.openFile("Choose file", null, null, false, false, textc::setText));
        result.c[2] = button;
        return result;
    }

    public DialogRow font(String key, String label) {
        DialogRow result = this.text(key, label);
        JButton button = new JButton("...");
        final JTextField textc = (JTextField) result.c[1];
        button.addActionListener(ev -> {
            FontDialog f = new FontDialog(Font.decode(textc.getText()));
            f.addFontChooseListener(textc::setText);
            f.show();
        });
        result.c[2] = button;
        return result;
    }

    public DialogRow color(String key, String label) {
        DialogRow result = this.text(key, label);
        final JTextField textc = (JTextField) result.c[1];
        final Color tempc;
        if (textc.getText() != null && textc.getText().length() > 0) {
            tempc = Color.decode(textc.getText());
        } else {
            tempc = Color.black;
        }

        final SolidIcon icon = new SolidIcon(tempc, 16, 16);
        JButton button = new JButton(icon);
        button.addActionListener(ev -> SafeDialogs.chooseColor("pick a color", tempc, r -> {
            textc.setText(r);
            icon.setColor(Color.decode(r));
        }));
        result.c[2] = button;
        return result;
    }

    public DialogRow file_import(final String key, String text, ATable table, final GenericTableModel model) {
        DialogRow result = this.file("_" + key, text);
        final JTextField textc = (JTextField) result.c[1];
        DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent ev) {
                this.check();
            }

            public void insertUpdate(DocumentEvent ev) {
                this.check();
            }

            public void removeUpdate(DocumentEvent ev) {
                this.check();
            }

            public void check() {
                model.clear(128);
                File file = new File(textc.getText().trim());
                if (file.exists() && file.canRead() && !file.isDirectory()) {
                    String[] data = CommonUtils.bString(CommonUtils.strrep(CommonUtils.readFile(file.getAbsolutePath()), "\r", "")).split("\n");

                    for (String aData : data)
                        if (aData.length() > 0) {
                            String[] temp = aData.split("\t");
                            if (temp.length == 1) {
                                model.addEntry((Map) CommonUtils.toMap("To", temp[0], "To_Name", ""));
                            } else if (temp.length >= 2) {
                                model.addEntry((Map) CommonUtils.toMap("To", temp[0], "To_Name", temp[1]));
                            }
                        }
                }

                model.fireListeners();
            }
        };
        textc.getDocument().addDocumentListener(listener);
        this.addDialogListenerInternal((event, options) -> options.put(key, model.export()));
        listener.insertUpdate(null);
        return result;
    }

    public JButton action(String text) {
        JButton button = this.action_noclose(text);
        button.addActionListener(ev -> {
            if (!DialogUtils.isShift(ev)) {
                DialogManager.this.dialog.setVisible(false);
                DialogManager.this.dialog.dispose();
            }

        });
        return button;
    }

    public JButton help(String url) {
        JButton button = new JButton("Help");
        button.addActionListener(DialogUtils.gotoURL(url));
        return button;
    }

    public DialogRow text(String key, String label) {
        return this.text(key, label, 20);
    }

    public DialogRow text_disabled(String key, String label) {
        DialogRow row = this.text(key, label, 20);
        row.get(1).setEnabled(false);
        return row;
    }

    public DialogRow text(final String key, String label, int cols) {
        final JTextField t = new ATextField(cols);
        if (this.options.containsKey(key)) {
            t.setText(this.options.get(key) + "");
        }

        this.addDialogListenerInternal((event, options) -> {
            if ("".equals(t.getText())) {
                options.put(key, "");
            } else {
                options.put(key, t.getText());
            }

        });
        DialogRow result = new DialogRow(new JLabel(label), t, new JPanel());
        this.rows.add(result);
        if (this.group != null) {
            this.group.add(result);
        }

        return result;
    }

    public DialogRow list_file(String key, String label) {
        return this.list(key, label, "file", 64);
    }

    public DialogRow list_text(String key, String label) {
        return this.list(key, label, "text", 160);
    }

    public DialogRow list(final String key, String label, String type, int height) {
        final JList<String> list = new JList<>();
        JScrollPane scroller = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.setPreferredSize(new Dimension(240, height));
        if (this.options.containsKey(key)) {
            String value = this.options.get(key) + "";
            if (!"".equals(value)) {
                list.setListData(value.split("!!"));
            }
        }

        this.addDialogListenerInternal((event, options) -> {
            if (list.getModel().getSize() == 0) {
                options.put(key, "");
            } else {
                LinkedList<String> results = IntStream.range(0, list.getModel().getSize()).mapToObj(x -> list.getModel().getElementAt(x)).collect(Collectors.toCollection(LinkedList::new));

                options.put(key, CommonUtils.join(results, "!!"));
            }

        });
        JButton del = new JButton(DialogUtils.getIcon("resources/cc/black/png/sq_minus_icon&16.png"));
        del.addActionListener(ev -> {
            LinkedList<String> results = new LinkedList<>();

            for (int x = 0; x < list.getModel().getSize(); ++x) {
                boolean selected = false;

                for (int z = 0; z < list.getSelectedIndices().length; ++z) {
                    if (x == list.getSelectedIndices()[z]) {
                        selected = true;
                    }
                }

                if (!selected) {
                    results.add(list.getModel().getElementAt(x));
                }
            }

            list.setListData(CommonUtils.toArray(results));
            DialogManager.this.options.put(key, CommonUtils.join(results, "!!"));
        });
        JComponent last;
        if ("file".equals(type)) {
            JButton add = new JButton(FileSystemView.getFileSystemView().getSystemIcon(new File(".")));
            add.addActionListener(ev -> SafeDialogs.openFile("Choose a file", null, null, false, false, r -> {
                LinkedList<String> results = IntStream.range(0, list.getModel().getSize()).mapToObj(x -> list.getModel().getElementAt(x)).collect(Collectors.toCollection(LinkedList::new));

                results.add(r);
                list.setListData(CommonUtils.toArray(results));
                DialogManager.this.options.put(key, CommonUtils.join(results, "!!"));
            }));
            last = DialogUtils.stack(add, del);
        } else {
            last = DialogUtils.stack(del);
        }

        DialogRow result = new DialogRow(new JLabel(label), scroller, last);
        this.rows.add(result);
        if (this.group != null) {
            this.group.add(result);
        }

        return result;
    }

    public DialogRow text_big(String key, String label) {
        return this.text_big(key, label, 20);
    }

    public DialogRow text_big(final String key, String label, int cols) {
        final JTextArea t = new JTextArea();
        t.setRows(3);
        t.setColumns(cols);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        if (this.options.containsKey(key)) {
            t.setText(this.options.get(key) + "");
        }

        this.addDialogListenerInternal((event, options) -> {
            if ("".equals(t.getText())) {
                options.put(key, "");
            } else {
                options.put(key, t.getText());
            }

        });
        DialogRow result = new DialogRow(new JLabel(label), new JScrollPane(t), new JPanel());
        this.rows.add(result);
        if (this.group != null) {
            this.group.add(result);
        }

        return result;
    }

    public DialogRow password(final String key, String label, int cols) {
        final JTextField t = new APasswordField(cols);
        if (this.options.containsKey(key)) {
            t.setText(this.options.get(key) + "");
        }

        this.addDialogListenerInternal((event, options) -> {
            if ("".equals(t.getText())) {
                options.remove(key);
            } else {
                options.put(key, t.getText());
            }

        });
        DialogRow result = new DialogRow(new JLabel(label), t, new JPanel());
        this.rows.add(result);
        if (this.group != null) {
            this.group.add(result);
        }

        return result;
    }

    public JComponent layout() {
        return this.layout(this.rows);
    }

    public JComponent layout(String groupn) {
        LinkedList<DialogRow> rows = this.groups.get(groupn);
        return this.layout(rows);
    }

    public JComponent row() {
        if (this.rows.size() != 1) {
            throw new RuntimeException("Can only layout a row with one component!");
        } else {
            DialogRow temp = this.rows.get(0);
            JPanel a = new JPanel();
            a.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            a.setLayout(new BorderLayout(5, 5));
            a.add(temp.get(0), "West");
            a.add(temp.get(1), "Center");
            a.add(temp.get(2), "East");
            return a;
        }
    }

    public JComponent layout(LinkedList<DialogRow> rows) {
        JPanel parent = new JPanel();
        GroupLayout layout = new GroupLayout(parent);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        parent.setLayout(layout);
        SequentialGroup hgroup = layout.createSequentialGroup();

        for (int x = 0; x < 3; ++x) {
            ParallelGroup pgroup = layout.createParallelGroup();

            for (DialogRow row : rows) {
                pgroup.addComponent(row.get(x));
            }

            hgroup.addGroup(pgroup);
        }

        layout.setHorizontalGroup(hgroup);
        SequentialGroup vgroup = layout.createSequentialGroup();

        for (DialogRow row : rows) {
            ParallelGroup pgroup = layout.createParallelGroup(Alignment.BASELINE);

            for (int x = 0; x < 3; ++x) {
                pgroup.addComponent(row.get(x));
            }

            vgroup.addGroup(pgroup);
        }

        layout.setVerticalGroup(vgroup);
        return parent;
    }

    public DialogRow checkbox_add(String key, String label, String description) {
        return this.checkbox_add(key, label, description, true);
    }

    public DialogRow checkbox_add(String key, String label, String description, boolean enabled) {
        JLabel lab = new JLabel(label);
        JCheckBox box = this.checkbox(key, description);
        if (!enabled) {
            lab.setEnabled(false);
            box.setEnabled(false);
        }

        DialogRow result = new DialogRow(lab, box, new JPanel());
        this.rows.add(result);
        if (this.group != null) {
            this.group.add(result);
        }

        return result;
    }

    public JCheckBox checkbox(final String key, String description) {
        final JCheckBox checkbox = new JCheckBox(description);
        if ("true".equals(this.options.get(key))) {
            checkbox.setSelected(true);
        } else {
            checkbox.setSelected(false);
        }

        this.addDialogListenerInternal((event, options) -> {
            if (checkbox.isSelected()) {
                options.put(key, "true");
            } else {
                options.put(key, "false");
            }

        });
        return checkbox;
    }

    private static class InterfaceAdd implements ActionListener, Callback {
        protected JComboBox<String> mybox;
        protected TeamQueue conn;
        protected DataManager data;

        public InterfaceAdd(JComboBox<String> b, TeamQueue c, DataManager d) {
            this.mybox = b;
            this.conn = c;
            this.data = d;
        }

        public void actionPerformed(ActionEvent ev) {
            InterfaceDialog dialog = new InterfaceDialog(this.conn, this.data);
            dialog.notify(this);
            dialog.show();
        }

        public void result(String call, final Object value) {
            CommonUtils.runSafe(() -> {
                InterfaceAdd.this.mybox.addItem(value + "");
                InterfaceAdd.this.mybox.setSelectedItem(value + "");
            });
        }
    }

    private static class ListenerAdd implements ActionListener, Callback, Runnable {
        protected JComboBox<String> mybox;
        protected TeamQueue conn;
        protected DataManager data;
        protected String value;

        public ListenerAdd(JComboBox<String> b, TeamQueue c, DataManager d) {
            this.mybox = b;
            this.conn = c;
            this.data = d;
        }

        public void actionPerformed(ActionEvent ev) {
            ListenerDialog dialog = new ListenerDialog(this.conn, this.data);
            dialog.notify(this);
            dialog.show();
        }

        public void run() {
            this.mybox.addItem(this.value);
            this.mybox.setSelectedItem(this.value);
        }

        public void result(String call, Object value) {
            this.value = value + "";
            CommonUtils.runSafe(this);
        }
    }

    public static final class DialogRow {
        public JComponent[] c = new JComponent[3];

        public DialogRow(JComponent a, JComponent b, JComponent c) {
            this.c[0] = a;
            this.c[1] = b;
            this.c[2] = c;
        }

        public JComponent get(int x) {
            return this.c[x];
        }

        public void last(JComponent cc) {
            this.c[2] = cc;
        }
    }
}
