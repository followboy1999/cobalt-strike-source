package common;

import javax.swing.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashSet;
import java.util.List;

public class Requirements {
    public static void recommended() {
    }

    public static HashSet<String> arguments() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        return new HashSet<>(arguments);
    }

    public static String requirements() {
        if ("1.6".equals(System.getProperty("java.specification.version"))) {
            return "Java 1.6 is not supported. Please upgrade to Java 1.7 or later.";
        }
        HashSet<String> results = Requirements.arguments();
        if (!results.contains("-XX:+AggressiveHeap")) {
            return "Java -XX:+AggressiveHeap option not set. Use the Cobalt Strike launcher. Don't click the .jar file!";
        }
        if (!results.contains("-XX:+UseParallelGC")) {
            return "Java -XX:+UseParallelGC option not set. Use the Cobalt Strike launcher. Don't click the .jar file!";
        }
        return null;
    }

    public static void checkGUI() {
        String env;
        Requirements.recommended();
        String error = Requirements.requirements();
        if (error != null) {
            JOptionPane.showMessageDialog(null, error, null, JOptionPane.ERROR_MESSAGE);
            CommonUtils.print_error(error);
            System.exit(0);
        }
        if ("wayland".equals(env = System.getenv("XDG_SESSION_TYPE"))) {
            CommonUtils.print_warn("You are using a Wayland desktop and not X11. Graphical Java applications run on Wayland are known to crash. You should use X11. See: https://www.cobaltstrike.com/help-wayland");
            JOptionPane.showInputDialog(null, "The Wayland desktop is not supported with Cobalt Strike.\nMore information:", null, JOptionPane.WARNING_MESSAGE, null, null, "https://www.cobaltstrike.com/help-wayland");
        }
    }

    public static void checkConsole() {
        Requirements.recommended();
        String error = Requirements.requirements();
        if (error != null) {
            CommonUtils.print_error(error);
            System.exit(0);
        }
    }
}

