package common;

import java.io.Serializable;
import java.util.Stack;

public class TranscriptReset implements Serializable,
        Scriptable {
    @Override
    public String eventName() {
        return "data_reset";
    }

    @Override
    public Stack eventArguments() {
        return new Stack();
    }
}

