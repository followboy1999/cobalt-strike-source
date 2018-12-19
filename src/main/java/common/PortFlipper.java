package common;

import java.util.Iterator;
import java.util.LinkedList;

public class PortFlipper {
    protected String ports;
    protected boolean hasError;
    protected String description;

    public boolean hasError() {
        return this.hasError;
    }

    public String getError() {
        return this.description;
    }

    public int check(int value) {
        if (value < 0 || value > 65535) {
            this.hasError = true;
            this.description = "Invalid port value '" + value + "'";
        }
        return value;
    }

    public LinkedList parse() {
        LinkedList<Integer> results = new LinkedList<>();
        String[] records = this.ports.split(",");
        for (String record : records) {
            if (CommonUtils.isNumber(record)) {
                results.add(this.check(CommonUtils.toNumber(record, -1)));
                continue;
            }
            if (record.matches("\\d+-\\d+")) {
                String[] v = record.split("-");
                int b = this.check(CommonUtils.toNumber(v[1], 0));
                for (int a = this.check(CommonUtils.toNumber(v[0], 0)); a <= b; ++a) {
                    results.add(a);
                }
                continue;
            }
            this.description = "Invalid port or range '" + record + "'";
            this.hasError = true;
        }
        return results;
    }

    public PortFlipper(String ports) {
        this.ports = ports;
    }

    private static void flip(byte[] mask, int value) {
        int index = value / 8;
        int bitpos = value % 8;
        mask[index] = (byte) (mask[index] + (1 << bitpos));
    }

    public Iterator iterator() {
        return this.parse().iterator();
    }

    public byte[] getMask() {
        byte[] temp = new byte[8192];
        Iterator i = this.iterator();
        while (i.hasNext()) {
            PortFlipper.flip(temp, (Integer) i.next());
        }
        return temp;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        Iterator i = this.iterator();
        while (i.hasNext()) {
            result.append(i.next());
            if (!i.hasNext()) continue;
            result.append(", ");
        }
        return result.toString();
    }
}

