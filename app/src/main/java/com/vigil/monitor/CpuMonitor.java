package com.vigil.monitor;

import java.time.Instant;

public class CpuMonitor extends Monitor<Double>{

    private final SystemMetricsProvider metrics;
    private final double telemetryDeadband;
    

    public CpuMonitor(SystemMetricsProvider metrics, double telemetryDeadband) {
        super("CPU");
        this.telemetryDeadband = telemetryDeadband;

        this.metrics = metrics;
    }

    @Override
    public MonitorReading<Double> read(){
        return new MonitorReading<Double>(
            this.getName(),
            metrics.cpuUsage(),
            Instant.now()
        );
    }

    @Override
    public double getTelemetryDeadband() {
        return this.telemetryDeadband;
    }
}
