package c2profile;

import common.CommonUtils;

import java.util.Set;

public class Checkers {
    public static boolean isComment(String a) {
        return a.charAt(0) == '#' && a.charAt(a.length() - 1) == '\n';
    }

    public static boolean isBlock(String a) {
        return a.charAt(0) == '{' && a.charAt(a.length() - 1) == '}';
    }

    public static boolean isString(String a) {
        return a.charAt(0) == '\"' && a.charAt(a.length() - 1) == '\"';
    }

    public static boolean isBoolean(String a) {
        return a.equals("true") || a.equals("false");
    }

    public static boolean isStatement(String a, String b) {
        return b.equals("EOT");
    }

    public static boolean isSetStatement(String a, String b, String c, String d) {
        return a.equals("set") && Checkers.isStatementArg(b, c, d);
    }

    public static boolean isIndicator(String a, String b, String c, String d) {
        return (a.equals("header") || a.equals("parameter") || a.equals("strrep")) && Checkers.isString(b) && Checkers.isString(c) && Checkers.isStatement(c, d);
    }

    public static boolean isStatementArg(String a, String b, String c) {
        return Checkers.isString(b) && Checkers.isStatement(a, c);
    }

    public static boolean isStatementBlock(String a, String b) {
        return Checkers.isBlock(b);
    }

    public static boolean isDate(String a) {
        return CommonUtils.isDate(a, "dd MMM yyyy HH:mm:ss");
    }

    public static boolean isNumber(String a) {
        try {
            Integer.parseInt(a);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isHTTPVerb(String a) {
        Set verbs = CommonUtils.toSet("GET, POST");
        return verbs.contains(a);
    }
}

