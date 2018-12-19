package socks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BeaconProxyListener implements ProxyListener {
    public byte[] writeMessage(int id, byte[] message, int length) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(256 + length);
        DataOutputStream data = new DataOutputStream(result);
        data.writeInt(15);
        data.writeInt(length + 4);
        data.writeInt(id);
        data.write(message, 0, length);
        return result.toByteArray();
    }

    public byte[] connectMessage(int id, String host, int port) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(256);
        DataOutputStream data = new DataOutputStream(result);
        data.writeInt(14);
        data.writeInt(host.length() + 6);
        data.writeInt(id);
        data.writeShort(port);
        data.writeBytes(host);
        return result.toByteArray();
    }

    public byte[] closeMessage(int id) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(256);
        DataOutputStream data = new DataOutputStream(result);
        data.writeInt(16);
        data.writeInt(4);
        data.writeInt(id);
        return result.toByteArray();
    }

    public byte[] listenMessage(int id, int port) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(256);
        DataOutputStream data = new DataOutputStream(result);
        data.writeInt(17);
        data.writeInt(6);
        data.writeInt(id);
        data.writeShort(port);
        return result.toByteArray();
    }

    @Override
    public void proxyEvent(SocksProxy server, ProxyEvent event) {
        try {
            switch (event.getType()) {
                case 1: {
                    byte[] args = this.connectMessage(event.getChannelId(), event.getHost(), event.getPort());
                    server.read(args);
                    break;
                }
                case 2: {
                    byte[] args = this.listenMessage(event.getChannelId(), event.getPort());
                    server.read(args);
                    break;
                }
                case 3: {
                    byte[] args = this.writeMessage(event.getChannelId(), event.getData(), event.getDataLength());
                    server.read(args);
                    break;
                }
                case 0: {
                    byte[] args = this.closeMessage(event.getChannelId());
                    server.read(args);
                }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }
}

