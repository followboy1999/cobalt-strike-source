package ui;

import common.CommonUtils;
import common.MudgeSanity;
import graph.Route;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Sorters {
    private static Set<String> hosts = new HashSet<>();
    private static Set<String> numbers = new HashSet<>();

    public static Comparator getStringSorter() {
        return new StringSorter();
    }

    public static Comparator getHostSorter() {
        return new HostSorter();
    }

    public static Comparator getNumberSorter() {
        return new NumberSorter();
    }

    public static Comparator getDateSorter(String format) {
        return new DateSorter(format);
    }

    public static Comparator getProperSorter(String col) {
        if (hosts.contains(col)) {
            return Sorters.getHostSorter();
        }
        if (numbers.contains(col)) {
            return Sorters.getNumberSorter();
        }
        return null;
    }

    static {
        hosts.add("external");
        hosts.add("host");
        hosts.add("Host");
        hosts.add("internal");
        hosts.add("session_host");
        hosts.add("address");
        numbers.add("last");
        numbers.add("pid");
        numbers.add("port");
        numbers.add("Port");
        numbers.add("sid");
        numbers.add("when");
        numbers.add("date");
        numbers.add("size");
        numbers.add("PID");
        numbers.add("PPID");
        numbers.add("Session");
    }

    private static class StringSorter implements Comparator {
        private StringSorter() {
        }

        public int compare(Object o1, Object o2) {
            if (o1 == null && o2 == null) {
                return this.compare("", "");
            }
            if (o1 == null) {
                return this.compare("", o2);
            }
            if (o2 == null) {
                return this.compare(o1, "");
            }
            return o1.toString().compareTo(o2.toString());
        }
    }

    private static class NumberSorter implements Comparator {
        private NumberSorter() {
        }

        public int compare(Object o1, Object o2) {
            long bb;
            String a = o1.toString();
            String b = o2.toString();
            long aa = CommonUtils.toLongNumber(a, 0L);
            if (aa == (bb = CommonUtils.toLongNumber(b, 0L))) {
                return 0;
            }
            if (aa > bb) {
                return 1;
            }
            return -1;
        }
    }

    private static class HostSorter implements Comparator {
        private HostSorter() {
        }

        public int compare(Object o1, Object o2) {
            long bb;
            String a = o1.toString();
            String b = o2.toString();
            if (a.equals("unknown")) {
                return this.compare("0.0.0.0", o2);
            }
            if (b.equals("unknown")) {
                return this.compare(o1, "0.0.0.0");
            }
            long aa = Route.ipToLong(a);
            if (aa == (bb = Route.ipToLong(b))) {
                return 0;
            }
            if (aa > bb) {
                return 1;
            }
            return -1;
        }
    }

    private static class DateSorter implements Comparator {
        protected SimpleDateFormat parser = null;

        public DateSorter(String format) {
            try {
                this.parser = new SimpleDateFormat(format);
            } catch (Exception ex) {
                MudgeSanity.logException("Parser: " + format, ex, false);
            }
        }

        public int compare(Object o1, Object o2) {
            long aa;
            long bb;
            String a = o1.toString();
            String b = o2.toString();
            try {
                aa = this.parser.parse(a).getTime();
            } catch (Exception ex) {
                aa = 0L;
            }
            try {
                bb = this.parser.parse(b).getTime();
            } catch (Exception ex) {
                bb = 0L;
            }
            return Long.compare(aa, bb);
        }
    }

}

