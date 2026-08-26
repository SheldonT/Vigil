package com.vigil.alarm;

import com.vigil.message.TelemetryOut;

public abstract class AlarmEvaluator<T> {
    
    public abstract Status evaluate(TelemetryOut<T> reading, MonitorState<T> currentState);

}
