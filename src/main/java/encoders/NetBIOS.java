package encoders;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class NetBIOS {
    public static String encode(char start, String source) {
        try {
            return NetBIOS.encode(start, source.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            return NetBIOS.encode(start, source.getBytes());
        }
    }

    public static String encode(char start, byte[] source) {
        StringBuilder sb = new StringBuilder();
        for (byte aSource : source) {
            int hi = (aSource & 240) >> 4;
            int low = aSource & 15;
            sb.append((char) (hi + (byte) start));
            sb.append((char) (low + (byte) start));
        }
        return sb.toString();
    }

    public static byte[] decode(char start, String source) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = 0; i < source.length(); i += 2) {
            char hi = source.charAt(i);
            char low = source.charAt(i + 1);
            byte me = (byte) (hi - (byte) start << 4);
            me = (byte) (me + (byte) (low - (byte) start));
            bout.write(me);
        }
        return bout.toByteArray();
    }

    public static void main(String[] args) {
        String e = NetBIOS.encode('A', "this is a test");
        System.err.println("Encode: " + e);
        System.err.println("Decode: '" + new String(NetBIOS.decode('A', e), StandardCharsets.UTF_8) + "'");
    }
}

