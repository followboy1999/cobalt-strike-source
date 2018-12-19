package graph;

public class Route {
    private static final long RANGE_MAX = Route.ipToLong("255.255.255.255");
    protected long begin;
    protected long end;
    protected String gateway;
    protected String network;
    protected String mask;

    public static long ipToLong(String address) {
        if (address == null) {
            return 0L;
        }
        String[] quads = address.split("\\.");
        long result = 0L;
        if (quads.length != 4) {
            return 0L;
        }
        try {
            result += (long) Integer.parseInt(quads[3]);
            result += Long.parseLong(quads[2]) << 8;
            result += Long.parseLong(quads[1]) << 16;
        } catch (Exception ex) {
            return result;
        }
        return result += Long.parseLong(quads[0]) << 24;
    }

    public Route(String address) {
        String[] description = address.split("/");
        String host;
        String network;
        if (description.length == 1) {
            host = address;
            String[] quads = address.split("\\.");
            network = quads[0].equals("0") ? "1" : (quads[1].equals("0") ? "8" : (quads[2].equals("0") ? "16" : (quads[3].equals("0") ? "24" : "32")));
        } else {
            host = description[0];
            network = description[1];
        }
        this.network = host;
        this.mask = network;
        this.gateway = "undefined";
        this.begin = Route.ipToLong(host);
        try {
            this.end = this.begin + (RANGE_MAX >> Integer.parseInt(network));
        } catch (Exception ex) {
            System.err.println(network + " is malformed!");
        }
    }

    public Route(String address, String networkMask, String gateway) {
        this.begin = Route.ipToLong(address);
        this.end = this.begin + (RANGE_MAX - Route.ipToLong(networkMask));
        this.gateway = gateway;
        this.network = address;
        this.mask = networkMask;
    }

    public boolean equals(Object o) {
        if (o instanceof Route) {
            Route p = (Route) o;
            return p.begin == this.begin && p.end == this.end && p.gateway.equals(this.gateway);
        }
        return false;
    }

    public int hashCode() {
        return (int) (this.begin + this.end + (long) this.gateway.hashCode());
    }

    public String getGateway() {
        return this.gateway;
    }

    public boolean shouldRoute(String address) {
        long check = Route.ipToLong(address);
        return check >= this.begin && check <= this.end;
    }

    public String toString() {
        return this.network + "/" + this.mask + " via " + this.gateway;
    }
}

