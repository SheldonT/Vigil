package com.vigil.alarm;

import com.vigil.monitor.TelemetryOut;

public abstract class AlarmEvaluator<T> {
    
    public abstract Status evaluate(TelemetryOut<T> reading, MonitorState<T> currentState);

}
