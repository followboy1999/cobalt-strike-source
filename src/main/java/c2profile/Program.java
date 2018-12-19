package c2profile;

import cloudstrike.Response;
import common.CommonUtils;
import common.MudgeSanity;
import dialog.DialogUtils;
import encoders.Base64;
import encoders.Base64Url;
import encoders.MaskEncoder;
import encoders.NetBIOS;

import java.io.*;
import java.util.*;

public class Program {
    protected Profile profile;
    protected boolean sealed = false;
    protected boolean posts = false;
    public static final int APPEND = 1;
    public static final int PREPEND = 2;
    public static final int BASE64 = 3;
    public static final int PRINT = 4;
    public static final int PARAMETER = 5;
    public static final int HEADER = 6;
    public static final int BUILD = 7;
    public static final int NETBIOS = 8;
    public static final int _PARAMETER = 9;
    public static final int _HEADER = 10;
    public static final int NETBIOSU = 11;
    public static final int URI_APPEND = 12;
    public static final int BASE64URL = 13;
    public static final int STRREP = 14;
    public static final int MASK = 15;
    protected List<Statement> tsteps = new LinkedList<>();
    protected LinkedList<Statement> rsteps = new LinkedList<>();

    public boolean isSealed() {
        return this.sealed;
    }

    public boolean postsData() {
        return this.posts;
    }

    public void addStep(String action, String argument) {
        Statement s = new Statement();
        s.argument = argument;
        if (argument != null) {
            s.alen = argument.length();
        }
        switch (action) {
            case "append":
                s.action = APPEND;
                break;
            case "prepend":
                s.action = PREPEND;
                break;
            case "base64":
                s.action = BASE64;
                break;
            case "print":
                s.action = PRINT;
                this.sealed = true;
                this.posts = true;
                break;
            case "parameter":
                s.action = PARAMETER;
                this.sealed = true;
                break;
            case "header":
                s.action = HEADER;
                this.sealed = true;
                break;
            case "build":
                s.action = BUILD;
                break;
            case "netbios":
                s.action = NETBIOS;
                break;
            case "!parameter":
                s.action = _PARAMETER;
                break;
            case "!header":
                s.action = _HEADER;
                break;
            case "netbiosu":
                s.action = NETBIOSU;
                break;
            case "uri-append":
                s.action = URI_APPEND;
                this.sealed = true;
                break;
            case "base64url":
                s.action = BASE64URL;
                break;
            case "strrep":
                s.action = STRREP;
                break;
            case "mask":
                s.action = MASK;
                break;
            default:
                throw new RuntimeException("Invalid action: " + action);
        }
        this.tsteps.add(s);
        this.rsteps.addFirst(s);
    }

    public Program(Profile profile) {
        this.profile = profile;
    }

    public byte[] transform_binary() throws IOException {
        ByteArrayOutputStream store = new ByteArrayOutputStream(1024);
        DataOutputStream data = new DataOutputStream(store);
        this.transform_binary(data);
        return store.toByteArray();
    }

    public void transform_binary(DataOutputStream data) throws IOException {
        Iterator<Statement> i = this.tsteps.iterator();
        while (true) {
            label84:
            while (i.hasNext()) {
                Program.Statement next = i.next();
                int x;
                switch (next.action) {
                    case 1:
                        data.writeInt(1);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label84;
                            }
                            data.write((byte) next.argument.charAt(x));
                            ++x;
                        }
                    case 2:
                        data.writeInt(2);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label84;
                            }
                            data.write((byte) next.argument.charAt(x));
                            ++x;
                        }
                    case 3:
                        data.writeInt(3);
                        break;
                    case 4:
                        data.writeInt(4);
                        break;
                    case 5:
                        data.writeInt(5);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label84;
                            }
                            data.write((byte) next.argument.charAt(x));
                            ++x;
                        }
                    case 6:
                        data.writeInt(6);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label84;
                            }
                            data.write((byte) next.argument.charAt(x));
                            ++x;
                        }
                    case 7:
                        data.writeInt(7);
                        if (next.argument.endsWith("metadata")) {
                            data.writeInt(0);
                        } else if (next.argument.endsWith("id")) {
                            data.writeInt(0);
                        } else if (next.argument.endsWith("output")) {
                            data.writeInt(1);
                        } else {
                            System.err.println("UNKNOWN DATA ARGUMENT: " + next.argument);
                        }
                        this.profile.getProgram(next.argument).transform_binary(data);
                        break;
                    case 8:
                        data.writeInt(8);
                        break;
                    case 9:
                        data.writeInt(9);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label84;
                            }
                            data.write((byte) next.argument.charAt(x));
                            ++x;
                        }
                    case 10:
                        data.writeInt(10);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label84;
                            }
                            data.write((byte) next.argument.charAt(x));
                            ++x;
                        }
                    case 11:
                        data.writeInt(11);
                        break;
                    case 12:
                        data.writeInt(12);
                        break;
                    case 13:
                        data.writeInt(13);
                    case 14:
                    default:
                        break;
                    case 15:
                        data.writeInt(15);
                }
            }
            return;
        }
    }

    public void transform(Response mydata, byte[] content) {
        SmartBuffer buffer = new SmartBuffer();
        buffer.append(content);
        this.transform(mydata, buffer);
    }

    public void transform(Response r, SmartBuffer buffer) {
        for (Statement tstep : this.tsteps) {
            Statement next = tstep;
            String encoded;
            switch (next.action) {
                case 1:
                    buffer.append(toBytes(next.argument));
                    break;
                case 2:
                    buffer.prepend(toBytes(next.argument));
                    break;
                case 3:
                    encoded = Base64.encode(buffer.getBytes());
                    buffer.clear();
                    buffer.append(toBytes(encoded));
                    break;
                case 4:
                    byte[] me = buffer.getBytes();
                    r.data = new ByteArrayInputStream(me);
                    r.size = (long) me.length;
                    r.offset = (long) buffer.getDataOffset();
                    r.addHeader("Content-Length", me.length + "");
                    break;
                case 5:
                    r.addParameter(next.argument + "=" + toBinaryString(buffer.getBytes()));
                    break;
                case 6:
                    r.addHeader(next.argument, toBinaryString(buffer.getBytes()));
                    break;
                case 7:
                    if (".http-post.client.output".equals(next.argument)) {
                        SmartBuffer bufferz = new SmartBuffer();
                        bufferz.append(CommonUtils.randomData(16));
                        this.profile.getProgram(next.argument).transform(r, bufferz);
                    } else {
                        this.profile.getProgram(next.argument).transform(r, buffer);
                    }
                    break;
                case 8:
                    encoded = NetBIOS.encode('a', buffer.getBytes());
                    buffer.clear();
                    buffer.append(toBytes(encoded));
                    break;
                case 9:
                    r.addParameter(next.argument);
                    break;
                case 10:
                    r.addHeader(next.argument);
                    break;
                case 11:
                    encoded = NetBIOS.encode('A', buffer.getBytes());
                    buffer.clear();
                    buffer.append(toBytes(encoded));
                    break;
                case 12:
                    r.uri = toBinaryString(buffer.getBytes());
                    break;
                case 13:
                    encoded = Base64Url.encode(buffer.getBytes());
                    buffer.clear();
                    buffer.append(toBytes(encoded));
                    break;
                case 14:
                default:
                    System.err.println("Unknown: " + next);
                    break;
                case 15:
                    encoded = toBinaryString(MaskEncoder.encode(buffer.getBytes()));
                    buffer.clear();
                    buffer.append(toBytes(encoded));
            }
        }
    }

    public byte[] recover_binary() throws IOException {
        ByteArrayOutputStream store = new ByteArrayOutputStream(1024);
        DataOutputStream data = new DataOutputStream(store);
        Iterator<Statement> i = this.rsteps.iterator();
        while (true) {
            label45:
            while (i.hasNext()) {
                Program.Statement next = i.next();
                int x;
                switch (next.action) {
                    case 1:
                        data.writeInt(1);
                        data.writeInt(next.alen);
                        break;
                    case 2:
                        data.writeInt(2);
                        data.writeInt(next.alen);
                        break;
                    case 3:
                        data.writeInt(3);
                        break;
                    case 4:
                        data.writeInt(4);
                        break;
                    case 5:
                        data.writeInt(5);
                        data.writeInt(next.alen);
                        x = 0;
                        while (true) {
                            if (x >= next.argument.length()) {
                                continue label45;
                            }
                            data.write(next.argument.charAt(x));
                            ++x;
                        }
                    case 6:
                        data.writeInt(5);
                        data.writeInt(next.alen);
                        for (x = 0; x < next.argument.length(); ++x) {
                            data.write(next.argument.charAt(x));
                        }
                    case 7:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    default:
                        break;
                    case 8:
                        data.writeInt(8);
                        break;
                    case 11:
                        data.writeInt(11);
                        break;
                    case 13:
                        data.writeInt(13);
                        break;
                    case 15:
                        data.writeInt(15);
                }
            }
            return store.toByteArray();
        }
    }

    public List<String> collissions() {
        HashSet<String> temp = new HashSet<String>();
        HashMap<String, String> prev = new HashMap<String, String>();
        LinkedList<String> results = new LinkedList<String>();
        this.collissions(null, temp, prev, results);
        return results;
    }

    private static String[] split(String arg) {
        String[] cand = arg.split("[:=]");
        if (cand.length != 2) {
            return new String[]{arg, ""};
        }
        cand[1] = cand[1].trim();
        return cand;
    }

    public void collissions(String program, Set<String> temp, Map<String, String> prev, List<String> results) {
        for (Statement tstep : this.tsteps) {
            String what = null;
            String where = null;
            Statement next = tstep;
            String[] t;
            switch (next.action) {
                case 12: {
                    what = "uri-append";
                    where = "block '" + program + "'";
                    break;
                }
                case 4: {
                    what = "print";
                    where = "block '" + program + "'";
                    break;
                }
                case 9: {
                    t = Program.split(next.argument);
                    what = "parameter " + t[0];
                    where = "value '" + t[1] + "'";
                    break;
                }
                case 5: {
                    what = "parameter " + next.argument;
                    where = "block '" + program + "'";
                    break;
                }
                case 10: {
                    t = Program.split(next.argument);
                    what = "header " + t[0];
                    where = "value '" + t[1] + "'";
                    break;
                }
                case 6: {
                    what = "header " + next.argument;
                    where = "block '" + program + "'";
                    break;
                }
                case 7: {
                    this.profile.getProgram(next.argument).collissions(next.argument, temp, prev, results);
                }
            }
            if (what == null) continue;
            if (temp.contains(what)) {
                results.add(what + ": " + where + ", " + prev.get(what));
                continue;
            }
            temp.add(what);
            prev.put(what, where);
        }
    }

    private static String toBinaryString(byte[] data) {
        try {
            return new String(data, "ISO8859-1");
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }

    public static byte[] toBytes(String data) {
        int length = data.length();
        byte[] r = new byte[length];
        for (int x = 0; x < length; ++x) {
            r[x] = (byte) data.charAt(x);
        }
        return r;
    }

    public String recover(Map headers, Map parameters, String input, String uri) {
        String data = "";
        for (Statement rstep : this.rsteps) {
            Statement next = rstep;
            switch (next.action) {
                case 1:
                    try {
                        data = data.substring(0, data.length() - next.alen);
                        break;
                    } catch (RuntimeException var12) {
                        MudgeSanity.logException("substr('" + data.replaceAll("\\P{Print}", ".") + "', 0, " + data.length() + " - " + next.alen + ")", var12, false);
                        return "";
                    }
                case 2:
                    try {
                        data = data.substring(next.alen);
                        break;
                    } catch (RuntimeException var11) {
                        MudgeSanity.logException("substr('" + data.replaceAll("\\P{Print}", ".") + "', " + next.alen + ", " + data.length() + ")", var11, false);
                        return "";
                    }
                case 3:
                    try {
                        data = toBinaryString(Base64.decode(data));
                        break;
                    } catch (RuntimeException var10) {
                        MudgeSanity.logException("base64 decode: " + data, var10, true);
                        return "";
                    }
                case 4:
                    data = input;
                    break;
                case 5:
                    data = DialogUtils.string(parameters, next.argument);
                    break;
                case 6:
                    data = CommonUtils.getCaseInsensitive(headers, next.argument, "");
                case 7:
                case 9:
                case 10:
                    break;
                case 8:
                    data = toBinaryString(NetBIOS.decode('a', data));
                    break;
                case 11:
                    data = toBinaryString(NetBIOS.decode('A', data));
                    break;
                case 12:
                    data = uri;
                    break;
                case 13:
                    try {
                        data = toBinaryString(Base64Url.decode(data));
                        break;
                    } catch (RuntimeException var9) {
                        MudgeSanity.logException("base64url decode: " + data, var9, true);
                        return "";
                    }
                case 14:
                default:
                    System.err.println("Unknown: " + next);
                    break;
                case 15:
                    data = toBinaryString(MaskEncoder.decode(CommonUtils.toBytes(data)));
            }
        }
        return data;
    }

    public byte[] transformData(byte[] data) {
        if (this.tsteps.size() == 0) {
            return data;
        } else {
            SmartBuffer buffer = new SmartBuffer();
            buffer.append(data);
            for (Statement tstep : this.tsteps) {
                Statement next = tstep;
                switch (next.action) {
                    case 1:
                        buffer.append(toBytes(next.argument));
                        break;
                    case 2:
                        buffer.prepend(toBytes(next.argument));
                        break;
                    case 14:
                        String orig = next.argument.substring(0, next.argument.length() / 2);
                        String repl = next.argument.substring(next.argument.length() / 2);
                        buffer.strrep(orig, repl);
                        break;
                    default:
                        System.err.println("Unknown: " + next);
                }
            }
            return buffer.getBytes();
        }
    }

    public byte[] getPrependedData() {
        if (this.tsteps.size() == 0) {
            return new byte[0];
        } else {
            SmartBuffer buffer = new SmartBuffer();
            for (Statement tstep : this.tsteps) {
                Statement next = tstep;
                switch (next.action) {
                    case 2:
                        buffer.prepend(toBytes(next.argument));
                }
            }
            return buffer.getBytes();
        }
    }

    public byte[] getAppendedData() {
        if (this.tsteps.size() == 0) {
            return new byte[0];
        } else {
            SmartBuffer buffer = new SmartBuffer();
            for (Statement tstep : this.tsteps) {
                Statement next = tstep;
                switch (next.action) {
                    case 1:
                        buffer.append(toBytes(next.argument));
                }
            }
            return buffer.getBytes();
        }
    }

    public static final class Statement {
        public String argument = "";
        public int action = 0;
        public int alen = 0;

        public String toString() {
            return "(" + this.action + ":" + this.argument + ")";
        }
    }
}
