package common;

import aggressor.Aggressor;

import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MudgeSanity {
    private static Map<String, String> details = new HashMap<>();

    public MudgeSanity() {
    }

    public static void logException(String activity, Throwable ex, boolean expected) {
        if (expected) {
            CommonUtils.print_warn("Trapped " + ex.getClass().getName() + " during " + activity + " [" + Thread.currentThread().getName() + "]: " + ex.getMessage());
        } else {
            CommonUtils.print_error("Trapped " + ex.getClass().getName() + " during " + activity + " [" + Thread.currentThread().getName() + "]: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    public static void systemDetail(String key, String value) {
        details.put(key, value);
    }

    public static String systemInformation() {
        StringBuilder output = new StringBuilder();
        output.append("== Cobalt Strike Properties ==\n\n");
        output.append("Is trial: ").append(License.isTrial()).append("\n");
        output.append("Version:  ").append(Aggressor.VERSION).append("\n");
        output.append("\n\n== Java Properties ==\n\n");
        Iterator i = System.getProperties().entrySet().iterator();

        while (i.hasNext()) {
            Entry next = (Entry) i.next();
            if (!"sun.java.command".equals(next.getKey())) {
                output.append(next.getKey()).append(" ").append(next.getValue()).append("\n");
            }
        }

        Set notneeded = CommonUtils.toSet("XDG_SESSION_COOKIE, LS_COLORS, TERMCAP");
        output.append("\n\n== Environment ==\n\n");
        i = System.getenv().entrySet().iterator();

        while (i.hasNext()) {
            Entry next = (Entry) i.next();
            if (!notneeded.contains(next.getKey())) {
                output.append(next.getKey()).append("=").append(next.getValue()).append("\n");
            }
        }

        output.append("\n\n== Security Providers ==\n\n");
        Provider[] provs = Security.getProviders();

        for (Provider prov : provs) {
            output.append(prov.toString()).append("\n");
        }

        if (details.size() > 0) {
            output.append("\n\n== Other ==\n\n");
            i = details.entrySet().iterator();

            while (i.hasNext()) {
                Entry next = (Entry) i.next();
                output.append(next.getKey()).append(" ").append(next.getValue()).append("\n");
            }
        }

        return output.toString();
    }

    public static void debugJava() {
        CommonUtils.writeToFile(new File("debug.txt"), CommonUtils.toBytes(systemInformation()));
        CommonUtils.print_info("saved debug.txt");
    }

    public static void debugRequest(String tx, Map<Object, Object> headers, Map<Object, Object> parameters, String input, String uri, String ext) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("A Malleable C2 attempt to recover data from a '").append(tx).append("' transaction failed. This could be due to a bug in the profile, a change made to the profile after this Beacon was run, or a change made to the transaction by some device between your target and your Cobalt Strike controller. The following information will (hopefully) help narrow down what happened.\n\n");
        buffer.append("From   '").append(ext).append("'\n");
        buffer.append("URI    '").append(uri).append("'\n");
        if (input != null && !"".equals(input)) {
            buffer.append("post'd '").append(input.replaceAll("\\P{Print}", ".")).append("'\n");
        }

        Iterator<Entry<Object, Object>> i;
        Entry<Object, Object> entry;
        if (headers != null && headers.size() > 0) {
            buffer.append("\nHeaders\n");
            buffer.append("-------\n");
            i = headers.entrySet().iterator();

            while (i.hasNext()) {
                entry = i.next();
                buffer.append("'").append(entry.getKey()).append("' = '").append(entry.getValue()).append("'\n");
            }
        }

        if (parameters != null && parameters.size() > 0) {
            buffer.append("\nParameters\n");
            buffer.append("----------\n");
            i = parameters.entrySet().iterator();

            while (i.hasNext()) {
                entry = i.next();
                buffer.append("'").append(entry.getKey()).append("' = '").append(entry.getValue()).append("'\n");
            }
        }

        CommonUtils.print_error(buffer.toString());
    }
}
