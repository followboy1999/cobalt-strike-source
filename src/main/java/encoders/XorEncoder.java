package encoders;

import common.CommonUtils;
import common.MudgeSanity;
import common.Packer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class XorEncoder {
    public static byte[] encode(byte[] data) {
        try {
            Packer result = new Packer();
            DataInputStream source = new DataInputStream(new ByteArrayInputStream(CommonUtils.pad(data)));
            int nonce = CommonUtils.rand(Integer.MAX_VALUE);
            result.addInt(nonce);
            result.little();
            result.addIntWithMask(source.available(), nonce);
            result.big();
            while (source.available() > 0) {
                nonce = source.readInt() ^ nonce;
                result.addInt(nonce);
            }
            source.close();
            return result.getBytes();
        } catch (IOException ioex) {
            MudgeSanity.logException("encode: " + data.length + " bytes", ioex, false);
            return new byte[0];
        }
    }

    public static void main(String[] args) {
        String temp = "\u00ccthis is a test and should show up after decoding.";
        byte[] data = CommonUtils.toBytes(temp);
        System.err.println(CommonUtils.toNasmHexString(XorEncoder.encode(data)));
    }
}

