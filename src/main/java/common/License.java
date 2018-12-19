package common;

import aggressor.Prefs;

public class License {
    private static long life;
    private static long today;
    private static long start;
    private static long difference;

    private static long getTimeSinceStart() {
        Prefs prefs = Prefs.getPreferences();
        today = System.currentTimeMillis();
        start = prefs.getLongNumber("cobaltstrike.start.int", 0L);
        if (start == 0L) {
            prefs.set("cobaltstrike.start.int", today + "");
            prefs.save();
            start = today;
        }
        difference = (today - start) / 86400000L;
        return difference;
    }

    public static void checkLicenseGUI(Authorization authorization) {
    }

    public static boolean isTrial() {
        return false;
    }

    public static void checkLicenseConsole(Authorization authorization) {
    }

    static {
        License.life = 99999L;
        License.today = 0L;
        License.start = 0L;
        License.difference = 0L;
    }
}

