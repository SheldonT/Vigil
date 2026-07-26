package com.vigil.monitor;

import java.time.Instant;

public class ProcessCpuUsage extends Monitor<Double>{

    private final SystemMetricsProvider metrics;
    private final double telemetryDeadband;

    public ProcessCpuUsage(SystemMetricsProvider metrics, double telemetryDeadband) {
        super("ProcessCpuUsage");

        this.metrics = metrics;
        this.telemetryDeadband = telemetryDeadband;
    }

    @Override
    public MonitorReading<Double> read(){
        return new MonitorReading<Double>(
            this.getName(),
            metrics.processCpuUsage(),
            Instant.now()
        );
    }

    @Override
    public double getTelemetryDeadband() {
        return this.telemetryDeadband;
    }

}
