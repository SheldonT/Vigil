package com.vigil.monitor;

public class SystemLoadAverage extends Monitor{

    private final SystemMetricsProvider metrics;

    public SystemLoadAverage(SystemMetricsProvider metrics) {
        super("SystemLoadAverage");

        this.metrics = metrics;
    }

    @Override
    public double get(){
        return metrics.systemLoadAverage();
    }

}
