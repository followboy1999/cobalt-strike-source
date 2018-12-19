package dialog;

import common.CommonUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SafeDialogs {
    protected static File lastSaveDirectory = null;
    protected static File lastOpenDirectory = null;

    public static void askYesNo(final String text, final String title, final SafeDialogCallback callback) {
        CommonUtils.runSafe(() -> {
            int result = JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION);
            if (result == 0 || result == 0) {
                SafeDialogs.post(callback, "yes");
            }
        });
    }

    private static void post(final SafeDialogCallback callback, final String text) {
        new Thread(() -> callback.dialogResult(text), "dialog result thread").start();
    }

    public static void ask(final String text, final String initial, final SafeDialogCallback callback) {
        CommonUtils.runSafe(() -> {
            String result = JOptionPane.showInputDialog(text, initial);
            if (result != null) {
                SafeDialogs.post(callback, result);
            }
        });
    }

    public static void saveFile(final JFrame frame, final String selection, final SafeDialogCallback callback) {
        CommonUtils.runSafe(() -> {
            File file;
            JFileChooser fc = new JFileChooser();
            if (selection != null) {
                fc.setSelectedFile(new File(selection));
            }
            if (SafeDialogs.lastSaveDirectory != null) {
                fc.setCurrentDirectory(SafeDialogs.lastSaveDirectory);
            }
            if (fc.showSaveDialog(frame) == 0 && (file = fc.getSelectedFile()) != null) {
                SafeDialogs.lastSaveDirectory = file.isDirectory() ? file : file.getParentFile();
                SafeDialogs.post(callback, file + "");
            }
        });
    }

    public static void chooseColor(final String title, final Color defaultv, final SafeDialogCallback callback) {
        CommonUtils.runSafe(() -> {
            Color val = JColorChooser.showDialog(null, title, defaultv);
            if (val != null) {
                SafeDialogs.post(callback, DialogUtils.encodeColor(val));
            }
        });
    }

    public static void openFile(final String title, final String sel, final String dir, final boolean multi, final boolean dirsonly, final SafeDialogCallback callback) {
        CommonUtils.runSafe(() -> {
            JFileChooser fc = new JFileChooser();
            if (title != null) {
                fc.setDialogTitle(title);
            }
            if (sel != null) {
                fc.setSelectedFile(new File(sel));
            }
            if (dir != null) {
                fc.setCurrentDirectory(new File(dir));
            } else if (SafeDialogs.lastOpenDirectory != null) {
                fc.setCurrentDirectory(SafeDialogs.lastOpenDirectory);
            }
            fc.setMultiSelectionEnabled(multi);
            if (dirsonly) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            if (fc.showOpenDialog(null) != 0) {
                return;
            }
            if (multi) {
                StringBuilder buffer = new StringBuilder();
                File[] r = fc.getSelectedFiles();
                for (int x = 0; x < r.length; ++x) {
                    if (r[x] == null || !r[x].exists()) continue;
                    buffer.append(r[x]);
                    if (x + 1 >= r.length) continue;
                    buffer.append(",");
                }
                SafeDialogs.post(callback, buffer.toString());
            } else {
                if (fc.getSelectedFile() != null && fc.getSelectedFile().exists()) {
                    SafeDialogs.lastOpenDirectory = fc.getSelectedFile().isDirectory() ? fc.getSelectedFile() : fc.getSelectedFile().getParentFile();
                }
                SafeDialogs.post(callback, fc.getSelectedFile() + "");
            }
        });
    }

}

