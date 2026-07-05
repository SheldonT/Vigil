package com.vigil.monitor;

public class CpuMonitor extends Monitor{

    private final SystemMetricsProvider metrics;

    public CpuMonitor(SystemMetricsProvider metrics, double warningHi, double alarmHi, double warningLo, double alarmLo) {
        super("CPU", warningHi, alarmHi, warningLo, alarmLo);

        this.metrics = metrics;
    }

    @Override
    public double get(){
        return metrics.cpuUsage();
    }

    @Override
    public MonitorResult check() {
        double value = this.get();

        Status status;

        if (value >= this.getAlarmHi()) {
            status = Status.HIGH_ALARM;
        } else if (value >= getWarningHi()) {
            status = Status.HIGH_WARNING;
        } else {
            status = Status.OK;
        }

        return new MonitorResult(getName(), value, status);
    }
}
