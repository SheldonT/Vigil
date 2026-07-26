package com.vigil.monitor;

import java.time.Instant;

public class SystemLoadAverage extends Monitor<Double>{

    private final SystemMetricsProvider metrics;
        private final double telemetryDeadband;

    public SystemLoadAverage(SystemMetricsProvider metrics, double telemetryDeadband) {
        super("SystemLoadAverage");

        this.metrics = metrics;
        this.telemetryDeadband = telemetryDeadband;
    }

    @Override
    public MonitorReading<Double> read(){
        return new MonitorReading<Double>(
            this.getName(),
            metrics.systemLoadAverage(),
            Instant.now()
        );
    }

    @Override
    public double getTelemetryDeadband() {
        return this.telemetryDeadband;
    }

}
