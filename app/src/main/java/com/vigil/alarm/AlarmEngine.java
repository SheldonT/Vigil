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

    private final Map<String, AlarmState<?>> alarmStates = new HashMap<>();


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

        if (status != Status.OK) {
            AlarmResult<T> initialAlarm = new AlarmResult<>(
                monitor.getName(),
                reading.value(),
                status,
                now
            );
            this.alarmStates.put(monitor.getName(), new AlarmState<>(initialAlarm));
        }

        return initialState;
    }

    public <T> AlarmMessage<T> evaluate(MonitorReading<T> value, AlarmEvaluator<T> evaluator) {

        State<T> monitorState = getState(value.name());

        Status newStatus = evaluator.evaluate(value, monitorState);
        Instant now = Instant.now();

        AlarmMessage<T> message = null;
        
        if (newStatus != monitorState.getStatus()) {

            monitorState.transitionTo(newStatus, now);
            AlarmResult<T> event = new AlarmResult<T>(value.name(), value.value(), newStatus, now);
            AlarmState<T> updatedAlarmState = this.updateAlarmState(event);

            if (updatedAlarmState != null) {
                message = new AlarmMessage<>(
                    updatedAlarmState.getAlarmId(),
                    value.name(),
                    value.value(),
                    updatedAlarmState.getCurrentStatus(),
                    updatedAlarmState.getAcknowledged(),
                    updatedAlarmState.getActivatedAt(),
                    updatedAlarmState.getAcknowledgedAt(),
                    updatedAlarmState.getLastUpdated()
                );
            }
        }

        monitorState.setValue(value.value());
        monitorState.setLastEvaluated(now);
        
        return message;
    }

    private <T> AlarmState<T> updateAlarmState(AlarmResult<T> result){

        AlarmState<T> current = getAlarmState(result.name());

        if (result.status() == Status.OK) {
            if (current == null) {
                return null;
            }

            current.update(result);
            alarmStates.remove(result.name());
            return current;
        }

        if (current == null) {
            AlarmState<T> created = new AlarmState<>(result);
            alarmStates.put(result.name(), created);
            return created;
        }

        current.update(result);
        return current;
    }

    @SuppressWarnings("unchecked")
    private <T> State<T> getState(String monitorName) {
        return (State<T>) this.monitorStates.get(monitorName);
    }

    @SuppressWarnings("unchecked")
    private <T> AlarmState<T> getAlarmState(String monitorName) {
        return (AlarmState<T>) this.alarmStates.get(monitorName);
    }
    
}
