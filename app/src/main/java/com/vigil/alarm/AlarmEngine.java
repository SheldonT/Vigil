package com.vigil.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.MonitorReading;

public class AlarmEngine {

    //private final Map<String, NumericAlarmConfig> alarmConfigs;
    private final Map<String, State<?>> monitorStates = new HashMap<>();


    public AlarmEngine(List<Monitor<?>> monitors) {

        //this.alarmConfigs = alarmConfigs;

        for (Monitor<?> m : monitors){
            // if (!alarmConfigs.containsKey(m.getName())){
            //     throw new IllegalStateException("Missing alarm configs for " + m.getName());
            // }

            State<?> initialState = initializeState(m);
            this.monitorStates.put(m.getName(), initialState);
        }
    }
    
    private <T> State<T> initializeState(Monitor<T> monitor){

        MonitorReading<T> reading = monitor.read();
        AlarmEvaluator<T> evaluator = monitor.getAlarmEvaluator();
        State<T> initialState = new State<>(monitor.getName());
        Instant now = Instant.now();
        Status status = evaluator.evaluate(reading, initialState);

        initialState.setState(status, reading.value(), now);
        initialState.setLastEvaluated(now);

        return initialState;
    }

    public <T> AlarmResult<T> evaluate(MonitorReading<T> value, AlarmEvaluator<T> evaluator) {

        State<T> state = getState(value.name());

        Status newStatus = evaluator.evaluate(value, state);

        AlarmResult<T> event = null;

        Instant now = Instant.now();

        if (newStatus != state.getStatus() ) {
            event = new AlarmResult<>(value.name(), value.value(), newStatus, now);

            state.transitionTo(newStatus, now);
        }

        state.setValue(value.value());
        state.setLastEvaluated(now);

        return event;
    }

    @SuppressWarnings("unchecked")
    private <T> State<T> getState(String monitorName) {
        return (State<T>) this.monitorStates.get(monitorName);
    }
    
}
