package com.vigil.monitor;

public class MonitorResult {
    public final String name;
    public final double value;
    public final Status status;

    public MonitorResult(String name, double value, Status status) {
        this.name = name;
        this.value = value;
        this.status = status;
    }
}
