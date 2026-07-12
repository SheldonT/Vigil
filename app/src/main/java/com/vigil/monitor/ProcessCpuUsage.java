package com.vigil.monitor;

public class ProcessCpuUsage extends Monitor{

    private final SystemMetricsProvider metrics;

    public ProcessCpuUsage(SystemMetricsProvider metrics) {
        super("ProcessCpuUsage");

        this.metrics = metrics;
    }

    @Override
    public double get(){
        return metrics.processCpuUsage();
    }

}
