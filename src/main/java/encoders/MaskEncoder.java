package encoders;

import common.CommonUtils;
import common.DataParser;
import common.MudgeSanity;

public class MaskEncoder {
    public static byte[] decode(byte[] data) {
        try {
            byte[] result = new byte[data.length - 4];
            DataParser parser = new DataParser(data);
            byte[] key = parser.readBytes(4);
            for (int x = 0; x < result.length && parser.more(); ++x) {
                result[x] = (byte) (parser.readByte() ^ key[x % 4]);
            }
            return result;
        } catch (Throwable ioex) {
            MudgeSanity.logException("'mask' decode [" + data.length + " bytes] failed", ioex, false);
            return new byte[0];
        }
    }

    public static byte[] encode(byte[] data) {
        byte[] stuff = new byte[data.length];
        byte[] key = new byte[]{(byte) CommonUtils.rand(255), (byte) CommonUtils.rand(255), (byte) CommonUtils.rand(255), (byte) CommonUtils.rand(255)};
        for (int x = 0; x < data.length; ++x) {
            stuff[x] = (byte) (data[x] ^ key[x % 4]);
        }
        return CommonUtils.join(key, stuff);
    }

    public static void main(String[] args) {
        String temp = "\u00ccthis is a test and should show up after decoding.";
        byte[] data = CommonUtils.toBytes(temp);
        System.err.println(CommonUtils.toNasmHexString(MaskEncoder.encode(data)));
    }
}

