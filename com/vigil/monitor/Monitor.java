package com.vigil.monitor;

import com.vigil.alarm.AlarmResult;

public abstract class Monitor {

    private final String name;

    private double warningHi;
    private double warningLo;

    private double alarmHi;
    private double alarmLo;

    public Monitor (String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public abstract double get();

    // public abstract AlarmResult check();
}