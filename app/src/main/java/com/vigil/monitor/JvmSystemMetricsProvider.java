package com.vigil.monitor;

public class JvmSystemMetricsProvider implements SystemMetricsProvider {

    private final com.sun.management.OperatingSystemMXBean os;

    public JvmSystemMetricsProvider(){
        this.os = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public double cpuUsage() {
        double v = os.getCpuLoad();

        return v < 0 ? 0 : v * 100;
    }

    @Override
    public double processCpuUsage() {
        double v = os.getProcessCpuLoad();

        return v < 0 ? 0 : v * 100;
    }

    @Override
    public double memoryUsage() {
        long total = os.getTotalMemorySize();
        long free = os.getFreeMemorySize();
        
        return ((double)(total - free) / total) * 100;
    }

    @Override
    public double systemLoadAverage() {
        return os.getSystemLoadAverage();
    }
}
