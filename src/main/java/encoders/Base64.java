package encoders;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Base64 {
    private static final char[] intToBase64 = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] base64ToInt = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    public static String encode(String source) {
        try {
            return Base64.encode(source.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return Base64.encode(source.getBytes());
        }
    }

    public static String encode(byte[] source) {
        int offset = 0;
        int num = 0;
        int numBytes = 0;
        StringBuilder sb = new StringBuilder();
        for (byte aSource : source) {
            int b;
            if ((b = source[offset++]) < 0) {
                b += 256;
            }
            num = (num << 8) + b;
            if (++numBytes != 3) continue;
            sb.append(intToBase64[num >> 18]);
            sb.append(intToBase64[num >> 12 & 63]);
            sb.append(intToBase64[num >> 6 & 63]);
            sb.append(intToBase64[num & 63]);
            num = 0;
            numBytes = 0;
        }
        if (numBytes > 0) {
            if (numBytes == 1) {
                sb.append(intToBase64[num >> 2]);
                sb.append(intToBase64[num << 4 & 63]);
                sb.append("==");
            } else {
                sb.append(intToBase64[num >> 10]);
                sb.append(intToBase64[num >> 4 & 63]);
                sb.append(intToBase64[num << 2 & 63]);
                sb.append('=');
            }
        }
        return sb.toString();
    }

    public static byte[] decode(String source) {
        int num = 0;
        int numBytes = 0;
        int eofBytes = 0;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        block6:
        for (int i = 0; i < source.length(); ++i) {
            byte result;
            char c = source.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (c == '=') {
                ++eofBytes;
                num <<= 6;
                switch (++numBytes) {
                    case 1:
                    case 2: {
                        throw new RuntimeException("Unexpected end of stream character (=)");
                    }
                    case 3: {
                        continue block6;
                    }
                    case 4: {
                        bout.write((byte) (num >> 16));
                        if (eofBytes != 1) continue block6;
                        bout.write((byte) (num >> 8));
                        continue block6;
                    }
                    case 5: {
                        throw new RuntimeException("Trailing garbage detected");
                    }
                    default: {
                        throw new IllegalStateException("Invalid value for numBytes");
                    }
                }
            }
            if (eofBytes > 0) {
                throw new RuntimeException("Base64 characters after end of stream character (=) detected.");
            }
            if (c >= '\u0000' && c < base64ToInt.length && (result = base64ToInt[c]) >= 0) {
                num = (num << 6) + result;
                if (++numBytes != 4) continue;
                bout.write((byte) (num >> 16));
                bout.write((byte) (num >> 8 & 255));
                bout.write((byte) (num & 255));
                num = 0;
                numBytes = 0;
                continue;
            }
            if (Character.isWhitespace(c)) continue;
            throw new RuntimeException("Invalid Base64 character: " + c);
        }
        return bout.toByteArray();
    }
}

