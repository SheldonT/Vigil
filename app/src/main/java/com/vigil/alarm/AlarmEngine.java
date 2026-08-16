package com.vigil.alarm;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.vigil.message.AlarmAcknowledgeOut;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.VigilMessage;
import com.vigil.monitor.Monitor;
import com.vigil.monitor.TelemetryOut;
import java.util.UUID;

public class AlarmEngine {

    //private final Map<String, NumericAlarmConfig> alarmConfigs;
    private final Map<String, MonitorState<?>> monitorStates = new HashMap<>();
    private final Map<String, AlarmState<?>> alarmStates = new HashMap<>();
    private final Queue<AlarmMessage<?>> startupAlarms = new ArrayDeque<>();
    private final Queue<AlarmAcknowledgeFail> ackFailQueue = new ConcurrentLinkedQueue<>();

    private AlarmAcknowledgeQueue ackQueue = new AlarmAcknowledgeQueue();
    
    public AlarmEngine(List<Monitor<?>> monitors) {

        //this.alarmConfigs = alarmConfigs;

        for (Monitor<?> m : monitors){
            // if (!alarmConfigs.containsKey(m.getName())){
            //     throw new IllegalStateException("Missing alarm configs for " + m.getName());
            // }

            MonitorState<?> initialState = initializeState(m);
            this.monitorStates.put(m.getName(), initialState);
        }
    }
    
    private <T> MonitorState<T> initializeState(Monitor<T> monitor){

        TelemetryOut<T> reading = monitor.read();
        AlarmEvaluator<T> evaluator = monitor.getAlarmEvaluator();
        MonitorState<T> initialState = new MonitorState<>(monitor.getName());
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
            AlarmState<T> alarmState = new AlarmState<>(initialAlarm);
            this.alarmStates.put(monitor.getName(), alarmState);
            this.startupAlarms.offer(alarmState.toMessage());
        }

        return initialState;
    }

    public <T> AlarmMessage<T> evaluate(TelemetryOut<T> value, AlarmEvaluator<T> evaluator) {

        MonitorState<T> monitorState = getState(value.name());

        Status newStatus = evaluator.evaluate(value, monitorState);
        Instant now = Instant.now();

        AlarmMessage<T> message = null;
        
        if (newStatus != monitorState.getStatus()) {

            monitorState.transitionTo(newStatus, now);
            AlarmResult<T> event = new AlarmResult<T>(value.name(), value.value(), newStatus, now);
            AlarmState<T> updatedAlarmState = this.updateAlarmState(event);

            if (updatedAlarmState != null) {
                message = updatedAlarmState.toMessage();
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

    public VigilMessage acknowledgeAlarm(UUID alarmId) {

        for (AlarmState<?> alarm : alarmStates.values()){
           if (alarm.getAlarmId().equals(alarmId)){
                alarm.acknowledge();
                this.ackQueue.submit(new AlarmAcknowledgeOut(alarmId, Instant.now(), alarm.getName()));
                
                return alarm.toMessage();
           }
        }

                AlarmAcknowledgeFail failure = new AlarmAcknowledgeFail(
                        alarmId,
                        "Alarm doesn't exist or already acknowledged"
                );
                this.ackFailQueue.offer(failure);
                return failure;
    }

    public AlarmMessage<?> pollStartupAlarm() {
        return this.startupAlarms.poll();
    }

    public AlarmAcknowledgeQueue getAckQueue(){
        return this.ackQueue;
    }

    public AlarmAcknowledgeFail pollAcknowledgeFail() {
        return this.ackFailQueue.poll();
    }

    @SuppressWarnings("unchecked")
    private <T> MonitorState<T> getState(String monitorName) {
        return (MonitorState<T>) this.monitorStates.get(monitorName);
    }

    @SuppressWarnings("unchecked")
    private <T> AlarmState<T> getAlarmState(String monitorName) {
        return (AlarmState<T>) this.alarmStates.get(monitorName);
    }
    
}
