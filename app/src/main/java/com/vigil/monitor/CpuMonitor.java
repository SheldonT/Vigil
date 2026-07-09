package com.vigil.monitor;

public class CpuMonitor extends Monitor{

    private final SystemMetricsProvider metrics;

    public CpuMonitor(SystemMetricsProvider metrics) {
        super("CPU");

        this.metrics = metrics;
    }

    @Override
    public double get(){
        return metrics.cpuUsage();
    }

}
