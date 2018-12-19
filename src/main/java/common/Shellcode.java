package common;

public class Shellcode {
    public static byte[] BindProtocolPackage(byte[] data) {
        Packer packer = new Packer();
        packer.little();
        packer.addInt(data.length);
        packer.addInt(data.length);
        packer.addString(data, data.length);
        return packer.getBytes();
    }
}

