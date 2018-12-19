package beacon;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CommandBuilder {
    protected ByteArrayOutputStream backing = new ByteArrayOutputStream(1024);
    protected DataOutputStream output = new DataOutputStream(this.backing);
    protected int command = 0;

    public void setCommand(int command) {
        this.command = command;
    }

    public void addStringArray(String[] data) {
        this.addShort(data.length);
        for (String aData : data) {
            this.addLengthAndString(aData);
        }
    }

    public void addString(String x) {
        try {
            this.backing.write(CommonUtils.toBytes(x));
        } catch (IOException ex) {
            MudgeSanity.logException("addString: '" + x + "'", ex, false);
        }
    }

    public void addString(byte[] x) {
        this.backing.write(x, 0, x.length);
    }

    public void addLengthAndString(String x) {
        this.addLengthAndString(CommonUtils.toBytes(x));
    }

    public void addLengthAndString(byte[] x) {
        try {
            if (x.length == 0) {
                this.addInteger(0);
            } else {
                this.addInteger(x.length);
                this.backing.write(x);
            }
        } catch (IOException ex) {
            MudgeSanity.logException("addLengthAndString: '" + x + "'", ex, false);
        }
    }

    public void addShort(int x) {
        byte[] bdata = new byte[8];
        ByteBuffer buffer = ByteBuffer.wrap(bdata);
        buffer.putShort((short) x);
        this.backing.write(bdata, 0, 2);
    }

    public void addByte(int x) {
        this.backing.write(x & 255);
    }

    public void addInteger(int x) {
        byte[] bdata = new byte[8];
        ByteBuffer buffer = ByteBuffer.wrap(bdata);
        buffer.putInt(x);
        this.backing.write(bdata, 0, 4);
    }

    public void pad(int len, int to) {
        while (len % 1024 != 0) {
            this.addByte(0);
            ++len;
        }
    }

    public byte[] build() {
        try {
            this.output.flush();
            byte[] args = this.backing.toByteArray();
            this.backing.reset();
            this.output.writeInt(this.command);
            this.output.writeInt(args.length);
            if (args.length > 0) {
                this.output.write(args, 0, args.length);
            }
            this.output.flush();
            byte[] result = this.backing.toByteArray();
            this.backing.reset();
            return result;
        } catch (IOException ioex) {
            MudgeSanity.logException("command builder", ioex, false);
            return new byte[0];
        }
    }
}

