package com.vigil.monitor;

import java.time.Instant;

public class MemoryMonitor extends Monitor<Double>{

    private final SystemMetricsProvider metrics;
    private final Double telemetryDeadband;

    public MemoryMonitor(SystemMetricsProvider metrics, Double telemetryDeadband) {
        super("Memory");

        this.metrics = metrics;
        this.telemetryDeadband = telemetryDeadband;
    }

    @Override
    public MonitorReading<Double> read(){
        return new MonitorReading<Double>(
            this.getName(),
            metrics.memoryUsage(),
            Instant.now()
        );
    }

    @Override
    public double getTelemetryDeadband() {
        return this.telemetryDeadband;
    }
}
