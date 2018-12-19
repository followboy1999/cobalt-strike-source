package common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser {
    protected String text;
    protected Matcher last = null;

    public RegexParser(String text) {
        this.text = text;
    }

    public static boolean isMatch(String text, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return m.matches();
    }

    public boolean matches(String pattern) {
        Matcher m;
        Pattern p = Pattern.compile(pattern);
        this.last = m = p.matcher(this.text);
        return m.matches();
    }

    public boolean endsWith(String texta) {
        if (this.text.endsWith(texta)) {
            this.text = this.text.substring(0, this.text.length() - texta.length());
            return true;
        }
        return false;
    }

    public String group(int groupno) {
        return this.last.group(groupno);
    }

    public void whittle(int groupno) {
        this.text = this.last.group(groupno);
    }

    public String getText() {
        return this.text;
    }
}

