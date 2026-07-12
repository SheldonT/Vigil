package com.vigil.monitor;

public class MemoryMonitor extends Monitor{

    private final SystemMetricsProvider metrics;

    public MemoryMonitor(SystemMetricsProvider metrics) {
        super("Memory");

        this.metrics = metrics;
    }

    @Override
    public double get(){
        return metrics.memoryUsage();
    }

}
