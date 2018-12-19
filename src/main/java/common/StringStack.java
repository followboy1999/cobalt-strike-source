package common;

import java.util.LinkedList;
import java.util.List;

public class StringStack {
    protected String string;
    protected String delimeter;

    public StringStack(String data) {
        this(data, " ");
    }

    public StringStack(String data, String delim) {
        this.string = data;
        this.delimeter = delim;
    }

    public List toList() {
        LinkedList<String> r = new LinkedList<>();
        StringStack temp = new StringStack(this.string, this.delimeter);
        while (!temp.isEmpty()) {
            r.add(temp.shift());
        }
        return r;
    }

    public void push(String element) {
        this.string = this.string.length() > 0 ? this.string + this.delimeter + element : element;
    }

    public int length() {
        return this.string.length();
    }

    public boolean isEmpty() {
        return this.string.length() == 0;
    }

    public String peekFirst() {
        if (this.string.contains(this.delimeter)) {
            return this.string.substring(0, this.string.indexOf(this.delimeter));
        }
        return this.string;
    }

    public String shift() {
        if (this.string.contains(this.delimeter)) {
            String temp = this.string.substring(0, this.string.indexOf(this.delimeter));
            if (temp.length() >= this.string.length()) {
                this.string = "";
                return temp;
            }
            this.string = this.string.substring(temp.length() + 1);
            return temp;
        }
        String temp = this.string;
        this.string = "";
        return temp;
    }

    public String pop() {
        int lasti = this.string.lastIndexOf(this.delimeter);
        if (lasti > -1) {
            String temp = this.string.substring(lasti + 1);
            this.string = this.string.substring(0, lasti);
            return temp;
        }
        String temp = this.string;
        this.string = "";
        return temp;
    }

    public String toString() {
        return this.string;
    }

    public void setDelimeter(String delim) {
        this.delimeter = delim;
    }
}

