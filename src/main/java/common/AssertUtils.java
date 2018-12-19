package common;

import java.util.Collection;
import java.util.Set;

public class AssertUtils {
    public static boolean Test(boolean value, String description) {
        if (!value) {
            return AssertUtils.TestFail(description);
        }
        return true;
    }

    public static boolean TestFail(String description) {
        CommonUtils.print_error("Assertion failed: " + description);
        Thread.currentThread();
        Thread.dumpStack();
        return false;
    }

    public static boolean TestNotNull(Object value, String description) {
        if (value == null) {
            CommonUtils.print_error("Assertion failed: " + description + " is null");
            Thread.currentThread();
            Thread.dumpStack();
            return false;
        }
        return true;
    }

    public static boolean TestUnique(Object value, Collection values) {
        if (values.contains(value)) {
            CommonUtils.print_error("Assertion failed: '" + value + "' is not unique in: " + values);
            Thread.currentThread();
            Thread.dumpStack();
            return false;
        }
        return true;
    }

    public static boolean TestSetValue(String candidate, String values) {
        Set temp = CommonUtils.toSet(values);
        if (temp.contains(candidate)) {
            return true;
        }
        CommonUtils.print_error("Assertion failed: '" + candidate + "' is not in: " + values);
        Thread.currentThread();
        Thread.dumpStack();
        return false;
    }

    public static boolean TestArch(String candidate) {
        return AssertUtils.TestSetValue(candidate, "x86, x64");
    }

    public static boolean TestPID(int candidate) {
        return AssertUtils.TestRange(candidate, 0, Integer.MAX_VALUE);
    }

    public static boolean TestPort(int candidate) {
        return AssertUtils.TestRange(candidate, 0, 65535);
    }

    public static boolean TestRange(int candidate, int low, int high) {
        if (candidate >= low && candidate <= high) {
            return true;
        }
        CommonUtils.print_error("Assertion failed: " + low + " <= " + candidate + " (value) <= " + high + " does not hold");
        Thread.currentThread();
        Thread.dumpStack();
        return false;
    }
}

