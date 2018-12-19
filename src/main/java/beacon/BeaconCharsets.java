package beacon;

import common.CommonUtils;
import common.MudgeSanity;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class BeaconCharsets {
    protected Map charsets_ansi = new HashMap();
    protected Map charsets_oem = new HashMap();

    protected BeaconCharsets() {
    }

    public String process(String id, byte[] text) {
        return this.process(this.charsets_ansi, id, text);
    }

    public String processOEM(String id, byte[] text) {
        return this.process(this.charsets_oem, id, text);
    }

    public String process(Map charsets, String id, byte[] text) {
        Charset foo = this.get(charsets, id);
        if (foo == null) {
            return CommonUtils.bString(text);
        }
        try {
            return foo.decode(ByteBuffer.wrap(text)).toString();
        } catch (Exception ex) {
            MudgeSanity.logException("could not convert text for id " + id + " with " + foo, ex, false);
            return CommonUtils.bString(text);
        }
    }

    public Charset get(Map charsets, String id) {
        synchronized (this) {
            return (Charset) charsets.get(id);
        }
    }

    public void register(String id, String charset_a, String charset_o) {
        this.register(this.charsets_ansi, id, charset_a);
        this.register(this.charsets_oem, id, charset_o);
    }

    public void register(Map charsets, String id, String charset) {
        if (charset == null) {
            return;
        }
        try {
            Charset foo = Charset.forName(charset);
            synchronized (this) {
                charsets.put(id, foo);
            }
        } catch (Exception ex) {
            MudgeSanity.logException("Could not find charset '" + charset + "' for Beacon ID " + id, ex, false);
        }
    }
}

