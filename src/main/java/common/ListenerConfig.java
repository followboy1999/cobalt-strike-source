package common;

import beacon.BeaconSetup;
import c2profile.Profile;

import java.io.Serializable;

public class ListenerConfig implements Serializable {
    protected boolean haspublic;
    protected String pname;
    protected String subhost;
    protected int txtlen;
    protected int garbage_bytes;
    protected String uri_x86;
    protected String uri_x64;
    protected String qstring;
    protected String headers;
    protected long stage_offset;
    protected String useragent;
    protected int watermark;

    public ListenerConfig(Profile profile) {
        this.pname = profile.getString(".pipename_stager");
        this.subhost = profile.getString(".dns_stager_subhost");
        this.haspublic = profile.option(".host_stage");
        this.useragent = BeaconSetup.randua(profile);
        this.uri_x86 = profile.getString(".http-stager.uri_x86");
        this.uri_x64 = profile.getString(".http-stager.uri_x64");
        this.qstring = profile.getQueryString(".http-stager.client");
        this.headers = profile.getHeaders(".http-stager.client");
        this.stage_offset = profile.getHTTPContentOffset(".http-stager.server");
        this.garbage_bytes = profile.getString(".bind_tcp_garbage").length();
        this.txtlen = profile.getString(".dns_stager_prepend").length();
        this.watermark = profile.getInt(".watermark");
    }

    public String pad(String start, int length) {
        StringBuilder result = new StringBuilder();
        result.append(start);
        while (result.length() < length) {
            if (this.watermark == 0) {
                // result.append("5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\u0000");
                continue;
            }
            result.append((char) CommonUtils.rand(255));
        }
        return result.toString().substring(0, length);
    }

    public String getWatermark() {
        Packer packer = new Packer();
        packer.addInt(this.watermark);
        return CommonUtils.bString(packer.getBytes());
    }

    public int getDNSOffset() {
        return this.txtlen;
    }

    public int getBindGarbageLength() {
        return this.garbage_bytes;
    }

    public long getHTTPStageOffset() {
        return this.stage_offset;
    }

    public String getHTTPHeaders() {
        return this.headers;
    }

    public String getQueryString() {
        return this.qstring;
    }

    public String getURI() {
        if (!"".equals(this.uri_x86)) {
            return this.uri_x86;
        }
        return CommonUtils.MSFURI();
    }

    public String getURI_X64() {
        if (!"".equals(this.uri_x64)) {
            return this.uri_x64;
        }
        return CommonUtils.MSFURI_X64();
    }

    public String getUserAgent() {
        return this.useragent;
    }

    public String getStagerPipe() {
        return this.pname;
    }

    public String getDNSSubhost() {
        return this.subhost;
    }

    public boolean hasPublicStage() {
        return this.haspublic;
    }
}

