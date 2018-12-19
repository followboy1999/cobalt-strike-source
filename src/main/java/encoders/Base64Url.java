package encoders;

public class Base64Url {
    public static String fix(String data) {
        if (data.endsWith("=")) {
            return Base64Url.fix(data.substring(0, data.length() - 1));
        }
        char[] temp = data.toCharArray();
        for (int x = 0; x < temp.length; ++x) {
            if (temp[x] == '/') {
                temp[x] = 95;
                continue;
            }
            if (temp[x] != '+') continue;
            temp[x] = 45;
        }
        return new String(temp);
    }

    public static String fix_reverse(String data) {
        char[] temp = data.toCharArray();
        for (int x = 0; x < temp.length; ++x) {
            if (temp[x] == '_') {
                temp[x] = 47;
                continue;
            }
            if (temp[x] != '-') continue;
            temp[x] = 43;
        }
        StringBuilder result = new StringBuilder(new String(temp));
        while (result.length() % 4 != 0) {
            result.append("=");
        }
        return result.toString();
    }

    public static String encode(byte[] source) {
        return Base64Url.fix(Base64.encode(source));
    }

    public static byte[] decode(String source) {
        return Base64.decode(Base64Url.fix_reverse(source));
    }
}

