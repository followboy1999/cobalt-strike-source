package common;

import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

public class ProfilerEvent implements Serializable,
        Transcript,
        Scriptable {
    public String external;
    public String internal;
    public String useragent;
    public Map applications;
    public String id;

    public ProfilerEvent(String external, String internal, String useragent, Map applications, String id) {
        this.external = external;
        this.internal = internal;
        this.useragent = useragent;
        this.applications = applications;
        this.id = id;
    }

    @Override
    public Stack eventArguments() {
        Stack<Scalar> temp = new Stack<>();
        temp.push(SleepUtils.getScalar(this.id));
        temp.push(SleepUtils.getHashWrapper(this.applications));
        temp.push(SleepUtils.getScalar(this.useragent));
        temp.push(SleepUtils.getScalar(this.internal));
        temp.push(SleepUtils.getScalar(this.external));
        return temp;
    }

    @Override
    public String eventName() {
        return "profiler_hit";
    }
}

