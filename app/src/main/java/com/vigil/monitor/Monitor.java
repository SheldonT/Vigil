package com.vigil.monitor;

import java.util.logging.Logger;

import com.vigil.alarm.AlarmEvaluator;

public abstract class Monitor<T> {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    private final String name;

    public Monitor (String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public double getTelemetryDeadband() {
        return 0.0;
    }

    public abstract TelemetryOut<T> read();

    public abstract AlarmEvaluator<T> getAlarmEvaluator();

}