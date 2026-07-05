package com.vigil.monitor;

public abstract class Monitor {

    private final String name;

    private double warningHi;
    private double warningLo;

    private double alarmHi;
    private double alarmLo;

    public Monitor (String name, double warningHi, double alarmHi, double warningLo, double alarmLo){
        this.name = name;
        this.warningHi = warningHi;
        this.warningLo = warningLo;
        this.alarmHi = alarmHi;
        this.alarmLo = alarmLo;
    }

    public String getName(){
        return this.name;
    }

    public double getWarningHi(){
        return this.warningHi;
    }

    public double getWarningLo(){
        return this.warningLo;
    }

    public double getAlarmHi(){
        return this.alarmHi;
    }

    public double getAlarmLo(){
        return this.alarmLo;
    }

    public abstract double get();

    public abstract MonitorResult check();
}