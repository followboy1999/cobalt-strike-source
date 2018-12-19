package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.CommonUtils;
import common.MudgeSanity;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class EncodedCommandBuilder
        extends CommandBuilder {
    protected AggressorClient client;

    public EncodedCommandBuilder(AggressorClient client) {
        this.client = client;
    }

    public byte[] process(String bid, String text) {
        String chst = this.getCharset(bid);
        try {
            Charset foo = Charset.forName(chst);
            if (foo == null) {
                return CommonUtils.toBytes(text);
            }
            ByteBuffer tempb = foo.encode(text);
            byte[] result = new byte[tempb.remaining()];
            tempb.get(result, 0, result.length);
            return result;
        } catch (Exception ex) {
            MudgeSanity.logException("could not convert text for id " + bid + " with " + chst, ex, false);
            return CommonUtils.toBytes(text);
        }
    }

    public String getCharset(String bid) {
        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
        if (entry != null) {
            return entry.getCharset();
        }
        return null;
    }

    public void addEncodedString(String bid, String x) {
        this.addString(this.process(bid, x));
    }

    public void addLengthAndEncodedString(String bid, String x) {
        this.addLengthAndString(this.process(bid, x));
    }
}

