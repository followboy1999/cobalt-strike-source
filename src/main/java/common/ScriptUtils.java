package common;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.runtime.Scalar;
import sleep.runtime.ScalarArray;
import sleep.runtime.SleepUtils;

import java.util.*;

public class ScriptUtils {
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

    public static Scalar convertAll(Object data) {
        if (data instanceof Collection) {
            Scalar temp = SleepUtils.getArrayScalar();
            for (Object o : ((Collection) data)) {
                temp.getArray().push(ScriptUtils.convertAll(o));
            }
            return temp;
        }
        if (data instanceof Map) {
            Scalar temp = SleepUtils.getHashScalar();
            for (Object o : ((Map) data).entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                Scalar key = SleepUtils.getScalar(entry.getKey() + "");
                Scalar value = temp.getHash().getAt(key);
                value.setValue(ScriptUtils.convertAll(entry.getValue()));
            }
            return temp;
        }
        if (data instanceof BeaconEntry) {
            return ScriptUtils.convertAll(((BeaconEntry) data).toMap());
        }
        if (data instanceof Scriptable) {
            Scriptable d = (Scriptable) data;
            Scalar temp = SleepUtils.getArrayScalar();
            temp.getArray().push(SleepUtils.getScalar(d.eventName()));
            Stack args = d.eventArguments();
            while (!args.isEmpty()) {
                temp.getArray().push((Scalar) args.pop());
            }
            return temp;
        }
        if (data instanceof ToScalar) {
            return ((ToScalar) data).toScalar();
        }
        if (data instanceof Object[]) {
            Object[] array2 = (Object[]) data;
            LinkedList<Object> result = new LinkedList<>();
            for (Object anArray2 : array2) {
                result.add(anArray2);
            }
            return ScriptUtils.convertAll(result);
        }
        return ObjectUtilities.BuildScalar(true, data);
    }

    public static String[] ArrayOrString(Stack args) {
        if (args.isEmpty()) {
            return new String[0];
        }
        Scalar temp = (Scalar) args.peek();
        if (temp.getArray() != null) {
            return CommonUtils.toStringArray(BridgeUtilities.getArray(args));
        }
        return new String[]{((Scalar) args.pop()).stringValue()};
    }

    public static Scalar IndexOrMap(Map vals, Stack args) {
        if (args.isEmpty()) {
            return SleepUtils.getHashWrapper(vals);
        }
        String key = BridgeUtilities.getString(args, "");
        return CommonUtils.convertAll(vals.get(key));
    }

    public static Stack StringToArguments(String args) {
        Stack<Scalar> tokens = new Stack<>();
        StringBuilder token = new StringBuilder();
        for (int x = 0; x < args.length(); ++x) {
            char temp = args.charAt(x);
            if (temp == ' ') {
                if (token.length() > 0) {
                    tokens.add(0, SleepUtils.getScalar(token.toString()));
                }
                token = new StringBuilder();
                continue;
            }
            if (temp == '\"' && token.length() == 0) {
                ++x;
                while (x < args.length() && args.charAt(x) != '\"') {
                    token.append(args.charAt(x));
                    ++x;
                }
                tokens.add(0, SleepUtils.getScalar(token.toString()));
                token = new StringBuilder();
                continue;
            }
            token.append(temp);
        }
        if (token.length() > 0) {
            tokens.add(0, SleepUtils.getScalar(token.toString()));
        }
        tokens.pop();
        return tokens;
    }
}

