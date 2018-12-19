package dns;

import common.CommonUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class DNSServer implements Runnable {
    protected DatagramSocket server;
    protected DatagramPacket in;
    protected DatagramPacket out;
    protected Handler listener;
    protected int ttl;
    public static final int DNS_TYPE_A = 1;
    public static final int DNS_TYPE_AAAA = 28;
    public static final int DNS_TYPE_CNAME = 5;
    public static final int DNS_TYPE_TXT = 16;
    protected boolean isup;

    public void setDefaultTTL(int _ttl) {
        this.ttl = _ttl;
    }

    public static Response A(long data) {
        Response r = new Response();
        r.type = DNS_TYPE_A;
        r.addr4 = data;
        return r;
    }

    public static Response TXT(byte[] data) {
        Response r = new Response();
        r.type = DNS_TYPE_TXT;
        r.data = data;
        return r;
    }

    public static Response AAAA(byte[] data) {
        Response r = new Response();
        r.type = DNS_TYPE_AAAA;
        r.data = data;
        return r;
    }

    public void installHandler(Handler l) {
        this.listener = l;
    }

    public byte[] respond(byte[] request) throws IOException {
        DNSAnswer a;
        ByteArrayOutputStream raw = new ByteArrayOutputStream(512);
        DataOutputStream out = new DataOutputStream(raw);
        DNSHeader header = new DNSHeader(request);
        header.flags = (short) header.flags | 32768;
        ++header.ancount;
        header.nscount = 0;
        header.arcount = 0;
        Iterator i = header.getQuestions().iterator();
        while (i.hasNext()) {
            DNSQuestion q = (DNSQuestion) i.next();
            a = new DNSAnswer(q);
            q.setAnswer(a);
        }
        i = header.getQuestions().iterator();
        while (i.hasNext()) {
            DNSQuestion q = (DNSQuestion) i.next();
            a = q.getAnswer();
            if (q.getType() != DNS_TYPE_AAAA || a.getType() != DNS_TYPE_A) continue;
            --header.ancount;
        }
        out.writeShort(header.id);
        out.writeShort(header.flags);
        out.writeShort(header.qdcount);
        out.writeShort(header.ancount);
        out.writeShort(header.nscount);
        out.writeShort(header.arcount);
        i = header.getQuestions().iterator();
        int start = 12;
        while (i.hasNext()) {
            DNSQuestion q = (DNSQuestion) i.next();
            out.write(request, start, q.getSize());
            start += q.getSize();
        }
        i = header.getQuestions().iterator();
        while (i.hasNext()) {
            DNSQuestion q = (DNSQuestion) i.next();
            DNSAnswer a2 = q.getAnswer();
            if (q.getType() == DNS_TYPE_AAAA && a2.getType() == DNS_TYPE_A) {
                CommonUtils.print_warn("Dropped AAAA request for: " + q.qname + " (A request expected)");
                continue;
            }
            out.write(a2.getAnswer());
        }
        out.close();
        return raw.toByteArray();
    }

    public DNSServer() throws IOException {
        this.in = new DatagramPacket(new byte[512], 512);
        this.out = new DatagramPacket(new byte[512], 512);
        this.listener = null;
        this.ttl = 1;
        this.isup = true;
        this.server = new DatagramSocket(53);
    }

    public void stop() {
        this.isup = false;
    }

    @Override
    public void run() {
        while (this.isup) {
            try {
                this.server.receive(this.in);
                DNSHeader header = new DNSHeader(this.in.getData());
                this.out.setAddress(this.in.getAddress());
                this.out.setPort(this.in.getPort());
                this.out.setData(this.respond(this.in.getData()));
                this.server.send(this.out);
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
        try {
            this.server.close();
        } catch (Exception ex) {
            // empty catch block
        }
        System.err.println("Killed DNS Server");
    }

    public void go() {
        new Thread(this).start();
    }

    private static class DNSHeader {
        public int id;
        public int flags;
        public int qdcount;
        public int ancount;
        public int nscount;
        public int arcount;
        protected LinkedList<DNSQuestion> questions = new LinkedList<>();

        public DNSHeader(byte[] raw) throws IOException {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(raw));
            this.id = data.readUnsignedShort();
            this.flags = data.readUnsignedShort();
            this.qdcount = data.readUnsignedShort();
            this.ancount = data.readUnsignedShort();
            this.nscount = data.readUnsignedShort();
            this.arcount = data.readUnsignedShort();
            for (int x = 0; x < this.qdcount; ++x) {
                DNSQuestion q = new DNSQuestion(data);
                this.questions.add(q);
            }
        }

        public LinkedList<DNSQuestion> getQuestions() {
            return this.questions;
        }

        public String toString() {
            String buffer = this.questions.stream().map(String::valueOf).collect(Collectors.joining("", "DNS Header\n" +
                    "ID:      " + Integer.toHexString(this.id) + "\n" +
                    "Flags:   " + Integer.toBinaryString(this.flags) + "\n" +
                    "QdCount: " + this.qdcount + "\n" +
                    "AnCount: " + this.ancount + "\n" +
                    "NsCount: " + this.nscount + "\n" +
                    "ArCount: " + this.arcount + "\n", ""));
            return buffer;
        }
    }

    private static class DNSQuestion {
        public String qname;
        public int qtype;
        public int qclass;
        public int size = 0;
        public DNSAnswer answer = null;

        public DNSQuestion(DataInputStream data) throws IOException {
            StringBuilder temp = new StringBuilder();
            int length = data.readUnsignedByte();
            ++this.size;
            while (length > 0) {
                for (int x = 0; x < length; ++x) {
                    temp.append((char) data.readUnsignedByte());
                    ++this.size;
                }
                temp.append(".");
                length = data.readUnsignedByte();
                ++this.size;
            }
            this.qname = temp.toString();
            this.qtype = data.readUnsignedShort();
            this.qclass = data.readUnsignedShort();
            this.size += 4;
        }

        public DNSAnswer getAnswer() {
            return this.answer;
        }

        public void setAnswer(DNSAnswer a) {
            this.answer = a;
        }

        public int getType() {
            return this.qtype;
        }

        public int getSize() {
            return this.size;
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("\tQuestion: '").append(this.qname).append("' size: ").append(this.size).append(" bytes\n");
            buffer.append("\tQType:    ").append(Integer.toHexString(this.qtype)).append("\n");
            buffer.append("\tQClass:   ").append(Integer.toHexString(this.qclass)).append("\n\n");
            return buffer.toString();
        }
    }

    private class DNSAnswer {
        protected ByteArrayOutputStream raw = new ByteArrayOutputStream(512);
        protected int type;

        public byte[] getAnswer() {
            return this.raw.toByteArray();
        }

        public int getType() {
            return this.type;
        }

        public DNSAnswer(DNSQuestion question) throws IOException {
            DataOutputStream output = new DataOutputStream(this.raw);
            String[] parts = question.qname.split("\\.");

            for (String part : parts) {
                output.writeByte(part.length());
                for (int y = 0; y < part.length(); ++y) {
                    output.writeByte(part.charAt(y));
                }
            }
            output.writeByte(0);
            if (DNSServer.this.listener != null) {
                Response r = DNSServer.this.listener.respond(question.qname, question.getType());
                if (r == null) {
                    System.err.println("Response for question is null\n" + question);
                    r = DNSServer.A(0L);
                }
                output.writeShort(r.type);
                output.writeShort(1);
                output.writeInt(DNSServer.this.ttl);
                this.type = r.type;
                int x;
                switch (r.type) {
                    case DNS_TYPE_A: {
                        output.writeShort(4);
                        output.writeInt((int) r.addr4);
                        break;
                    }
                    case DNS_TYPE_AAAA: {
                        output.writeShort(16);
                        for (x = 0; x < 16; ++x) {
                            if (x < r.data.length) {
                                output.writeByte(r.data[x]);
                                continue;
                            }
                            output.writeByte(0);
                        }
                        break;
                    }
                    case DNS_TYPE_TXT: {
                        if (r.data.length == 0) break;
                        output.writeShort(r.data.length + 1);
                        output.writeByte(r.data.length);
                        for (x = 0; x < r.data.length; ++x) {
                            output.writeByte(r.data[x]);
                        }
                        break;
                    }
                }
            }
            output.close();
        }
    }

    public interface Handler {
        Response respond(String var1, int var2);
    }

    public static final class Response {
        public int type = 0;
        public long addr4;
        public long[] addr6;
        public byte[] data;
    }

}

