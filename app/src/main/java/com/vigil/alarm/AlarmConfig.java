package com.vigil.alarm;

public class AlarmConfig {

    private final String monitorName;

    private final double highWarning;
    private final double highWarningClear;

    private final double highAlarm;
    private final double highAlarmClear;

    private final double lowWarning;
    private final double lowWarningClear;

    private final double lowAlarm;
    private final double lowAlarmClear;

    private final long activationDelayMs;
    private final long clearDelayMs;

    public AlarmConfig(String name,
                       double highWarn,
                       double highWarnClear,
                       double highAlarm,
                       double highAlarmClear,
                       double lowWarning,
                       double lowWarningClear,
                       double lowAlarm,
                       double lowAlarmClear,
                       long activationDelayMs,
                       long clearDelayMs) {

        this.monitorName = name;
    
        this.highWarning = highWarn;
        this.highWarningClear = highWarnClear;

        this.highAlarm = highAlarm;
        this.highAlarmClear = highAlarmClear;

        this.lowWarning = lowWarning;
        this.lowWarningClear = lowWarningClear;

        this.lowAlarm = lowAlarm;
        this.lowAlarmClear = lowAlarmClear;

        this.activationDelayMs = activationDelayMs;
        this.clearDelayMs = clearDelayMs;
    }

    public String getMonitorName() {
        return this.monitorName;
    }
    
    public double getHighWarning(){
        return this.highWarning;
    }
    
    public double getHighWarningClear(){
        return this.highWarningClear;
    }

    public double getHighAlarm(){
        return this.highAlarm;
    }
    
    public double getHighAlarmClear(){
        return this.highAlarmClear;
    }

    public double getLowWarning() {
        return this.lowWarning;
    }

    public double getLowWarningClear() {
        return this.lowWarningClear;
    }

    public double getLowAlarm() {
        return this.lowAlarm;
    }

    public double getLowAlarmClear() {
        return this.lowAlarmClear;
    }

    public long getActivationDelayMs() {
        return this.activationDelayMs;
    }
    
    public long getClearDelayMs() {
        return this.clearDelayMs;
    }
}
