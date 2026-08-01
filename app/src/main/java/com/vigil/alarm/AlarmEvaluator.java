package com.vigil.alarm;

import com.vigil.monitor.MonitorReading;

public abstract class AlarmEvaluator<T> {
    
    public abstract Status evaluate(MonitorReading<T> reading, State<T> currentState);

}
