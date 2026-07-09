package com.vigil.monitor;

public interface SystemMetricsProvider {
    double cpuUsage();
    double processCpuUsage();
    double memoryUsage();
    double systemLoadAverage();
}
