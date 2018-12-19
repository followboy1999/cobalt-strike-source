package dialog;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import aggressor.windows.BeaconConsole;
import aggressor.windows.SecureShellConsole;
import common.*;
import sleep.runtime.Scalar;
import table.FilterAndScroll;
import ui.ATable;
import ui.ATextField;
import ui.GenericTableModel;
import ui.Sorters;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class DialogUtils {
    public static final HashMap<String,Object> icache = new HashMap<>();

    public static JFrame dialog(String title, int width, int height) {
        JFrame dialog = new JFrame(title);
        dialog.setSize(width, height);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(null);
        dialog.setIconImage(DialogUtils.getImage("resources/armitage-icon.gif"));
        return dialog;
    }

    public static void showError(final String message) {
        CommonUtils.runSafe(() -> JOptionPane.showMessageDialog(null, message, null, JOptionPane.ERROR_MESSAGE));
    }

    public static void showInfo(final String message) {
        CommonUtils.runSafe(() -> JOptionPane.showMessageDialog(null, message, null, JOptionPane.INFORMATION_MESSAGE));
    }

    public static void showInput(final JFrame dialog, final String message, final String text) {
        CommonUtils.runSafe(() -> JOptionPane.showInputDialog(dialog, message, text));
    }

    public static GenericTableModel setupModel(String lead, String[] cols, List<Map<String, Object>> rows) {
        GenericTableModel model = new GenericTableModel(cols, lead, 8);
        for (Map<String, Object> row : rows) {
            model._addEntry(row);
        }
        return model;
    }

    public static Map<String, String> toMap(String description) {
        HashMap<String, String> result = new HashMap<>();
        StringStack stack = new StringStack(description, ",");
        while (!stack.isEmpty()) {
            String temp = stack.pop();
            String[] parts = temp.split(": ");
            if (parts.length != 2) {
                throw new RuntimeException("toMap: '" + description + "' failed at: " + temp);
            }
            result.put(parts[0].trim(), parts[1].trim());
        }
        return result;
    }

    public static void addToClipboard(String data) {
        StringSelection sel = new StringSelection(data);
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemSelection();
        if (cb != null) {
            cb.setContents(sel, null);
        }
        if ((cb = Toolkit.getDefaultToolkit().getSystemClipboard()) != null) {
            cb.setContents(sel, null);
        }
        DialogUtils.showInfo("Copied text to clipboard");
    }

    public static void setTableColumnWidths(JTable table, Map<String, String> widths) {
        for (Map.Entry<String, String> entry : widths.entrySet()) {
            String col = entry.getKey();
            int width = Integer.parseInt(entry.getValue());
            table.getColumn(col).setPreferredWidth(width);
        }
    }

    public static ATable setupTable(TableModel model, String[] cols, boolean multi) {
        if (multi) {
            return DialogUtils.setupTable(model, cols, 2);
        }
        return DialogUtils.setupTable(model, cols, 0);
    }

    public static ATable setupTable(TableModel model, String[] cols, int mode) {
        ATable table = new ATable(model);
        table.getSelectionModel().setSelectionMode(mode);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        for (int i = 0; i < cols.length; ++i) {
            String name = cols[i];
            Comparator sortme = Sorters.getProperSorter(name);
            if (sortme == null) continue;
            sorter.setComparator(i, sortme);
        }
        table.setRowSorter(sorter);
        return table;
    }

    public static void sortby(JTable table, int colno) {
        try {
            LinkedList<RowSorter.SortKey> sortKeys = new LinkedList<>();
            sortKeys.add(new RowSorter.SortKey(colno, SortOrder.ASCENDING));
            table.getRowSorter().setSortKeys(sortKeys);
            ((DefaultRowSorter) table.getRowSorter()).sort();
        } catch (Exception ex) {
            MudgeSanity.logException("sortby: " + colno, ex, false);
        }
    }

    public static void sortby(JTable table, int colno, int colno2) {
        try {
            LinkedList<RowSorter.SortKey> sortKeys = new LinkedList<>();
            sortKeys.add(new RowSorter.SortKey(colno, SortOrder.ASCENDING));
            sortKeys.add(new RowSorter.SortKey(colno2, SortOrder.ASCENDING));
            table.getRowSorter().setSortKeys(sortKeys);
            ((DefaultRowSorter) table.getRowSorter()).sort();
        } catch (Exception ex) {
            MudgeSanity.logException("sortby: " + colno + ", " + colno2, ex, false);
        }
    }

    public static void startedWebService(String type, String url) {
        final JFrame dialog = DialogUtils.dialog("Success", 240, 120);
        dialog.setLayout(new BorderLayout());
        JLabel label = new JLabel("<html>Started service: " + type + "<br />Copy and paste this URL to access it</html>");
        ATextField text = new ATextField(url, 20);
        JButton close = new JButton("Ok");
        close.addActionListener(ev -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        dialog.add(DialogUtils.wrapComponent(label, 5), "North");
        dialog.add(DialogUtils.wrapComponent(text, 5), "Center");
        dialog.add(DialogUtils.center(close), "South");
        dialog.pack();
        dialog.setVisible(true);
        dialog.setVisible(true);
    }

    public static void presentURL(String url) {
        final JFrame dialog = DialogUtils.dialog("Open URL", 240, 120);
        dialog.setLayout(new BorderLayout());
        JLabel label = new JLabel("I couldn't open your browser. Try browsing to:");
        ATextField text = new ATextField(url, 20);
        JButton close = new JButton("Ok");
        close.addActionListener(ev -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        dialog.add(DialogUtils.wrapComponent(label, 5), "North");
        dialog.add(DialogUtils.wrapComponent(text, 5), "Center");
        dialog.add(DialogUtils.center(close), "South");
        dialog.pack();
        dialog.setVisible(true);
    }

    public static void presentText(String title, String description, String content) {
        final JFrame dialog = DialogUtils.dialog(title, 240, 120);
        dialog.setLayout(new BorderLayout());
        JLabel label = new JLabel("<html>" + description + "</html>");
        ATextField text = new ATextField(content, 20);
        JButton close = new JButton("Ok");
        close.addActionListener(ev -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        dialog.add(DialogUtils.wrapComponent(label, 5), "North");
        dialog.add(DialogUtils.wrapComponent(text, 5), "Center");
        dialog.add(DialogUtils.center(close), "South");
        dialog.pack();
        dialog.setVisible(true);
    }

    public static JComponent wrapComponent(JComponent c, int margin) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(c, "Center");
        panel.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
        return panel;
    }

    public static JComponent pad(JComponent c, int left, int right, int top, int bottom) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(c, "Center");
        panel.setBorder(BorderFactory.createEmptyBorder(left, right, top, bottom));
        return panel;
    }

    private static LinkedList asList(JComponent a) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c, JComponent d) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        temp.add(d);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        temp.add(d);
        temp.add(e);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        temp.add(d);
        temp.add(e);
        temp.add(f);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f, JComponent g) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        temp.add(d);
        temp.add(e);
        temp.add(f);
        temp.add(g);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f, JComponent g, JComponent h) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        temp.add(d);
        temp.add(e);
        temp.add(f);
        temp.add(g);
        temp.add(h);
        return temp;
    }

    private static LinkedList asList(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f, JComponent g, JComponent h, JComponent i) {
        LinkedList<Object> temp = new LinkedList<>();
        temp.add(a);
        temp.add(b);
        temp.add(c);
        temp.add(d);
        temp.add(e);
        temp.add(f);
        temp.add(g);
        temp.add(h);
        temp.add(i);
        return temp;
    }

    public static JComponent stack(JComponent a) {
        return DialogUtils.stack(DialogUtils.asList(a));
    }

    public static JComponent stack(JComponent a, JComponent b) {
        return DialogUtils.stack(DialogUtils.asList(a, b));
    }

    public static JComponent stack(JComponent a, JComponent b, JComponent c) {
        return DialogUtils.stack(DialogUtils.asList(a, b, c));
    }

    public static JComponent stack(List components) {
        Box box = Box.createVerticalBox();
        for (Object component : components) {
            JComponent next = (JComponent) component;
            next.setAlignmentX(0.0f);
            box.add(next);
        }
        return box;
    }

    public static JComponent stackTwo(JComponent a, JComponent b) {
        JPanel temp = new JPanel();
        temp.setLayout(new BorderLayout());
        temp.add(a, "Center");
        temp.add(b, "South");
        return temp;
    }

    public static JComponent stackThree(JComponent a, JComponent b, JComponent c) {
        return DialogUtils.stackTwo(a, DialogUtils.stackTwo(b, c));
    }

    public static JComponent center(JComponent a) {
        return DialogUtils.center(DialogUtils.asList(a));
    }

    public static JComponent center(JComponent a, JComponent b) {
        return DialogUtils.center(DialogUtils.asList(a, b));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c) {
        return DialogUtils.center(DialogUtils.asList(a, b, c));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c, JComponent d) {
        return DialogUtils.center(DialogUtils.asList(a, b, c, d));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e) {
        return DialogUtils.center(DialogUtils.asList(a, b, c, d, e));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f) {
        return DialogUtils.center(DialogUtils.asList(a, b, c, d, e, f));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f, JComponent g) {
        return DialogUtils.center(DialogUtils.asList(a, b, c, d, e, f, g));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f, JComponent g, JComponent h) {
        return DialogUtils.center(DialogUtils.asList(a, b, c, d, e, f, g, h));
    }

    public static JComponent center(JComponent a, JComponent b, JComponent c, JComponent d, JComponent e, JComponent f, JComponent g, JComponent h, JComponent i) {
        return DialogUtils.center(DialogUtils.asList(a, b, c, d, e, f, g, h, i));
    }

    public static JComponent center(List components) {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        for (Object component : components) {
            JComponent next = (JComponent) component;
            p.add(next);
        }
        return p;
    }

    public static JComponent description(String text) {
        JEditorPane textarea = new JEditorPane();
        textarea.setContentType("text/html");
        textarea.setText(text.trim());
        textarea.setEditable(false);
        textarea.setOpaque(true);
        textarea.setCaretPosition(0);
        textarea.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        JScrollPane scroll = new JScrollPane(textarea);
        scroll.setPreferredSize(new Dimension(0, 48));
        scroll.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return scroll;
    }

    public static ActionListener gotoURL(final String url) {
        return ev -> {
            if (Desktop.isDesktopSupported()) {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().browse(new URL(url).toURI());
                    } catch (UnsupportedOperationException uoe) {
                        MudgeSanity.logException("goto: " + url + " *grumble* *grumble*", uoe, true);
                        DialogUtils.presentURL(url);
                    } catch (Exception ex) {
                        MudgeSanity.logException("goto: " + url, ex, false);
                    }
                }, "show URL").start();
            } else {
                CommonUtils.print_error("No desktop support to show: " + url);
            }
        };
    }

    public static boolean isShift(ActionEvent ev) {
        return (ev.getModifiers() & 1) == 1;
    }

    public static void setupTimeRenderer(JTable table, String col) {
        table.getColumn(col).setCellRenderer(ATable.getTimeTableRenderer());
    }

    public static void setupDateRenderer(JTable table, String col) {
        table.getColumn(col).setCellRenderer(ATable.getDateTableRenderer());
    }

    public static void setupSizeRenderer(JTable table, String col) {
        table.getColumn(col).setCellRenderer(ATable.getSizeTableRenderer());
    }

    public static void setupImageRenderer(JTable table, GenericTableModel model, String col, String imagecol) {
        table.getColumn(col).setCellRenderer(ATable.getImageTableRenderer(model, imagecol));
    }

    public static void setupBoldOnKeyRenderer(JTable table, GenericTableModel model, String col, String keycol) {
        table.getColumn(col).setCellRenderer(ATable.getBoldOnKeyRenderer(model, keycol));
    }

    public static void setupListenerStatusRenderer(JTable table, GenericTableModel model, String col) {
        table.getColumn(col).setCellRenderer(ATable.getListenerStatusRenderer(model));
    }

    public static boolean bool(Map options, String key) {
        String value = options.get(key) + "";
        return value.equals("true");
    }

    public static String string(Map options, String key) {
        Object value = options.get(key);
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public static int number(Map options, String key) {
        return Integer.parseInt(DialogUtils.string(options, key));
    }

    public static void workAroundEditorBug(JEditorPane editor) {
        editor.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
    }

    public static JComponent top(JComponent item) {
        JPanel temp = new JPanel();
        temp.setLayout(new BorderLayout());
        temp.add(item, "North");
        temp.add(Box.createVerticalGlue(), "Center");
        return temp;
    }

    public static String encodeColor(Color color) {
        StringBuilder val = new StringBuilder(Integer.toHexString(color.getRGB() & 16777215));
        while (val.length() < 6) {
            val.insert(0, "0");
        }
        return "#" + val;
    }

    public static JButton Button(String name, ActionListener l) {
        JButton button = new JButton(name);
        button.addActionListener(l);
        return button;
    }

    public static Icon getIcon(String name) {
        try {
            return new ImageIcon(ImageIO.read(CommonUtils.resource(name)));
        } catch (IOException ioex) {
            MudgeSanity.logException("getIcon: " + name, ioex, false);
            return null;
        }
    }

    public static Image getImage(String name) {
        try {
            return ImageIO.read(CommonUtils.resource(name));
        } catch (IOException ioex) {
            MudgeSanity.logException("getImage: " + name, ioex, false);
            return null;
        }
    }

    public static Image getImage(String[] images, boolean tint) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage buffered = gc.createCompatibleImage(1000, 776, 2);
        Graphics2D graphics = buffered.createGraphics();
        for (String image1 : images) {
            try {
                BufferedImage image = ImageIO.read(CommonUtils.resource(image1));
                graphics.drawImage(image, 0, 0, 1000, 776, null);
            } catch (Exception ex) {
                MudgeSanity.logException("getImage: " + image1, ex, false);
            }
        }
        if (tint) {
            graphics.setColor(Color.BLACK);
            AlphaComposite ac = AlphaComposite.getInstance(3, 0.4f);
            graphics.setComposite(ac);
            graphics.fillRect(0, 0, 1000, 776);
        }
        graphics.dispose();
        return buffered;
    }

    public static byte[] toImage(RenderedImage r, String format) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(524288);
            ImageIO.write(r, format, out);
            out.close();
            return out.toByteArray();
        } catch (Exception ex) {
            MudgeSanity.logException("toImage: " + format, ex, false);
            return new byte[0];
        }
    }

    public static Image getImageSmall(String[] images, boolean tint) {
        BufferedImage buffered = new BufferedImage(1000, 776, 2);
        Graphics2D graphics = buffered.createGraphics();
        for (String image1 : images) {
            try {
                BufferedImage image = ImageIO.read(CommonUtils.resource(image1));
                graphics.drawImage(image, 0, 0, 1000, 776, null);
            } catch (Exception ex) {
                MudgeSanity.logException("getImageSmall: " + image1, ex, false);
            }
        }
        if (tint) {
            float[] scales = new float[]{1.0f, 1.0f, 1.0f, 0.5f};
            float[] offsets = new float[4];
            RescaleOp op = new RescaleOp(scales, offsets, null);
            buffered = op.filter(buffered, null);
        }
        graphics.dispose();
        return buffered;
    }

    public static BufferedImage resize(Image source, int width, int height) {
        BufferedImage buffered = new BufferedImage(width, height, 2);
        Graphics2D graphics = buffered.createGraphics();
        Image image = source.getScaledInstance(width, height, 4);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return buffered;
    }

    public static void addToTable(final ATable table, final GenericTableModel model, final Map<String,Object> row) {
        CommonUtils.runSafe(() -> {
            table.markSelections();
            model.addEntry(row);
            model.fireListeners();
            table.restoreSelections();
        });
    }

    public static void setText(final ATextField text, final String data) {
        CommonUtils.runSafe(() -> text.setText(data));
    }

    public static void setTable(final ATable table, final GenericTableModel model, Collection<Map<String,Object>> rows) {
        if (!AssertUtils.TestNotNull(table, "table")) {
            return;
        }
        final LinkedList<Map<String,Object>> saferows = new LinkedList<>(rows);
        CommonUtils.runSafe(() -> {
            table.markSelections();
            model.clear(saferows.size());
            for (Map<String,Object> saferow : saferows) {
                model.addEntry(saferow);
            }
            model.fireListeners();
            table.restoreSelections();
        });
    }

    public static String[] TargetVisualizationArray(String os, double ver, boolean compromised) {
        String[] overlay = new String[]{"resources/unknown.png", compromised ? "resources/hacked.png" : "resources/computer.png"};
        if (os.equals("windows")) {
            if (ver <= 5.0) {
                overlay[0] = "resources/windows2000.png";
            } else if (ver > 5.0 && ver < 6.0) {
                overlay[0] = "resources/windowsxp.png";
            } else if (ver == 6.0 || ver == 6.1) {
                overlay[0] = "resources/windows7.png";
            } else if (ver >= 6.2) {
                overlay[0] = "resources/windows8.png";
            }
        } else {
            if (os.equals("firewall")) {
                return CommonUtils.toArray("resources/firewall.png");
            }
            if (os.equals("printer")) {
                return CommonUtils.toArray("resources/printer.png");
            }
            switch (os) {
                case "android":
                    overlay[0] = "resources/android.png";
                    break;
                case "vmware":
                    overlay[0] = "resources/vmware.png";
                    break;
                case "solaris":
                    overlay[0] = "resources/solaris.png";
                    break;
                case "freebsd":
                case "openbsd":
                case "netbsd":
                    overlay[0] = "resources/bsd.png";
                    break;
                case "linux":
                    overlay[0] = "resources/linux.png";
                    break;
                case "cisco ios":
                    overlay[0] = "resources/cisco.png";
                    break;
                case "macos x":
                    overlay[0] = "resources/macosx.png";
                    break;
                case "apple ios":
                    overlay[0] = "resources/ios.png";
                    break;
            }
        }
        return overlay;
    }

    public static ImageIcon TargetVisualizationSmall(String os, double ver, boolean compromised, boolean dead) {
        String key = "small:" + os.toLowerCase() + "." + ver + "." + compromised + "." + dead;
        synchronized (icache) {
            if (icache.containsKey(key)) {
                return (ImageIcon) icache.get(key);
            }
            Image result = DialogUtils.getImageSmall(DialogUtils.TargetVisualizationArray(os.toLowerCase(), ver, compromised), dead);
            ImageIcon small = new ImageIcon(result.getScaledInstance((int) Math.floor((double) result.getWidth(null) / 44.0), (int) Math.floor((double) result.getHeight(null) / 44.0), 4));
            icache.put(key, small);
            return small;
        }
    }

    public static Image TargetVisualizationMedium(String os, double ver, boolean compromised, boolean dead) {
        String key = "medium:" + os.toLowerCase() + "." + ver + "." + compromised + "." + dead;
        synchronized (icache) {
            if (icache.containsKey(key)) {
                return (Image) icache.get(key);
            }
            Image result = DialogUtils.getImageSmall(DialogUtils.TargetVisualizationArray(os.toLowerCase(), ver, compromised), dead);
            BufferedImage medium = DialogUtils.resize(result, 125, 97);
            icache.put(key, medium);
            return medium;
        }
    }

    public static Image TargetVisualization(String os, double ver, boolean compromised, boolean dead) {
        String key = os.toLowerCase() + "." + ver + "." + compromised + "." + dead;
        synchronized (icache) {
            if (icache.containsKey(key)) {
                return (Image) icache.get(key);
            }
            Image result = DialogUtils.getImage(DialogUtils.TargetVisualizationArray(os.toLowerCase(), ver, compromised), dead);
            icache.put(key, result);
            return result;
        }
    }

    public static void openOrActivate(final AggressorClient client, final String bid) {
        CommonUtils.runSafe(() -> {
            BeaconEntry entry = DataUtils.getBeacon(client.getData(), bid);
            if (entry.isEmpty() || entry.isSSH() || !client.getTabManager().activate(entry.title())) {
                if (entry.isBeacon()) {
                    BeaconConsole c = new BeaconConsole(bid, client);
                    client.getTabManager().addTab(entry.title(), c.getConsole(), c.cleanup(), "Beacon console");
                } else if (entry.isSSH()) {
                    SecureShellConsole c = new SecureShellConsole(bid, client);
                    client.getTabManager().addTab(entry.title(), c.getConsole(), c.cleanup(), "SSH console");
                }
            }
        });
    }

    public static void setupScreenshotShortcut(final AggressorClient client, final ATable table, final String title) {
        table.addActionForKey("ctrl pressed P", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                BufferedImage phear = table.getScreenshot();
                byte[] data = DialogUtils.toImage(phear, "png");
                client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot(title, data)));
                DialogUtils.showInfo("Pushed screenshot to team server");
            }
        });
    }

    public static byte[] screenshot(Component c) {
        BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), 6);
        Graphics g = image.getGraphics();
        c.paint(g);
        g.dispose();
        return DialogUtils.toImage(image, "png");
    }

    public static void removeBorderFromButton(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(2, 2, 2, 2));
    }

    public static JPanel FilterAndScroll(ATable tablez) {
        return new FilterAndScroll(tablez);
    }

    public static byte[] getStager(Map options) {
        String listener = DialogUtils.string(options, "listener");
        boolean is64 = DialogUtils.bool(options, "x64");
        byte[] result = new byte[]{};
        if (is64) {
            result = DataUtils.shellcodeX64(GlobalDataManager.getGlobalDataManager(), listener);
            if (result.length == 0) {
                DialogUtils.showError(listener + " does not have an x64 stager");
            }
        } else {
            result = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), listener);
        }
        return result;
    }

    public static void showSessionPopup(AggressorClient client, MouseEvent ev, Object[] values) {
        if (values.length == 0) {
            return;
        }
        String first = values[0].toString();
        String hook;
        hook = "beacon".equals(CommonUtils.session(first)) ? "beacon" : "ssh";
        Stack<Scalar> args = new Stack<>();
        args.push(CommonUtils.toSleepArray(values));
        client.getScriptEngine().getMenuBuilder().installMenu(ev, hook, args);
    }

}

