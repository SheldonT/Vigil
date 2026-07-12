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
        long available = memAvailable();
        return ((double)(total - available) / total) * 100;
    }

    private long memAvailable() {
        try {
            for (String line : java.nio.file.Files.readAllLines(java.nio.file.Path.of("/proc/meminfo"))) {
                if (line.startsWith("MemAvailable:")) {
                    // format: "MemAvailable:   7204852 kB"
                    return Long.parseLong(line.split("\\s+")[1]) * 1024;
                }
            }
        } catch (Exception ignored) {}
        return os.getFreeMemorySize();
    }

    @Override
    public double systemLoadAverage() {
        return os.getSystemLoadAverage();
    }
}
