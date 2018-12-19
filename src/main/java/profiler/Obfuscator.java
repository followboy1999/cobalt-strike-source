package profiler;

import java.util.UUID;

public class Obfuscator {
    protected String code;

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

    public static String garbage(String replaceme) {
        return replaceme.charAt(0) + Obfuscator._garbage(replaceme.substring(1));
    }

    public static String _garbage(String replaceme) {
        String garbage = Obfuscator.strrep(Obfuscator.ID(), "-", "");
        if (replaceme == null) {
            return "";
        }
        if (replaceme.length() > garbage.length()) {
            return garbage + Obfuscator.garbage(replaceme.substring(garbage.length()));
        }
        if (replaceme.length() == garbage.length()) {
            return garbage;
        }
        return garbage.substring(0, replaceme.length());
    }

    public static String ID() {
        return UUID.randomUUID().toString();
    }

    public Obfuscator(String code) {
        this.code = code;
    }

    public String obfuscate() {
        this.code = Obfuscator.strrep(this.code, "internalAddress", Obfuscator.garbage("internalAddress"));
        this.code = Obfuscator.strrep(this.code, "applications", Obfuscator.garbage("applications"));
        this.code = Obfuscator.strrep(this.code, "checkPlugin", Obfuscator.garbage("checkPlugin"));
        this.code = Obfuscator.strrep(this.code, "extractVersion", Obfuscator.garbage("extractVersion"));
        this.code = Obfuscator.strrep(this.code, "tokens", Obfuscator.garbage("tokens"));
        this.code = Obfuscator.strrep(this.code, "fixReaderVersion", Obfuscator.garbage("fixReaderVersion"));
        this.code = Obfuscator.strrep(this.code, "checkControl", Obfuscator.garbage("checkControl"));
        this.code = Obfuscator.strrep(this.code, "decloak", Obfuscator.garbage("decloak"));
        return this.code;
    }
}

