package com.vigil.alarm;

import java.time.Instant;

public class AlarmResult {
    public final String name;
    public final double value;
    public final Status status;
    public final Instant timestampNow;

    public AlarmResult(String name, double value, Status status, Instant tsNow) {
        this.name = name;
        this.value = value;
        this.status = status;
        this.timestampNow = tsNow;
    }
}
