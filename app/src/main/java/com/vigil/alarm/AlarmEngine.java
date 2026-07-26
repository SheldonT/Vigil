package com.vigil.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.Duration;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.MonitorReading;

public class AlarmEngine {

    private final Map<String, AlarmConfig> alarmConfigs;
    private Map<String, State> monitorStates = new HashMap<>();


    public AlarmEngine(List<Monitor<?>> monitors, Map<String, AlarmConfig> alarmConfigs) {
        this.alarmConfigs = alarmConfigs;

        for (Monitor<?> m : monitors){
            if (!alarmConfigs.containsKey(m.getName())){
                throw new IllegalStateException("Missing alarm configs for " + m.getName());
            }
            
            State initialState = initializeState(m, alarmConfigs.get(m.getName()));
            this.monitorStates.put(m.getName(), initialState);

        }

    }
    
    private State initializeState (Monitor<?> m, AlarmConfig config){


        MonitorReading<?> val = m.read();

        double initVal = (Double)val.value();

        State initState = new State (m.getName());

        Status status = this.determineRawStatus(initVal, config);

        initState.setState(status, initVal, Instant.now());

        return initState;
    }
    
    private Status determineRawStatus(double val, AlarmConfig config){

        Status status = Status.OK;

         if (val >= config.getHighAlarm()) {
            status = Status.HIGH_ALARM;
        } else if (val >= config.getHighWarning()) {
            status = Status.HIGH_WARNING;
        } else if (val <= config.getLowAlarm()) {
            status = Status.LOW_ALARM;
        } else if (val <= config.getLowWarning()) {
            status = Status.LOW_WARNING;
        }
        else {
            status = Status.OK;
        }

        return status;
    }

    private boolean pendingTimeElapsed(
        State state,
        Instant now,
        long delayMs) {

    return Duration.between(
        state.getPendingTime(),
        now
    ).toMillis() >= delayMs;
}

    private Status evaluateFromHighWarning(double value, State state, AlarmConfig config, Instant now){

        //check if we need to move to OK status
        if (value >= config.getHighAlarm()) {
            if (state.getPendingStatus() != Status.HIGH_ALARM){
                state.setPendingStatus(Status.HIGH_ALARM);
                state.setPendingTime(now);
            }
            
            if (this.pendingTimeElapsed(state, now, config.getActivationDelayMs())){

                return Status.HIGH_ALARM;
            }
        } else if (value <= config.getHighWarningClear()) {
            if (state.getPendingStatus() != Status.OK){
                state.setPendingStatus(Status.OK);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, config.getClearDelayMs())){

                return Status.OK;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.HIGH_WARNING;
    }

    private Status evaluateFromLowWarning(double value, State state, AlarmConfig config, Instant now){

        //check if we need to move to OK status
        if (value <= config.getLowAlarm()) {
            if (state.getPendingStatus() != Status.LOW_ALARM){
                state.setPendingStatus(Status.LOW_ALARM);
                state.setPendingTime(now);
            }
            
            if (this.pendingTimeElapsed(state, now, config.getActivationDelayMs())){

                return Status.LOW_ALARM;
            }
        } else if (value >= config.getLowWarningClear()) {
            if (state.getPendingStatus() != Status.OK){
                state.setPendingStatus(Status.OK);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, config.getClearDelayMs())){

                return Status.OK;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.LOW_WARNING;
    }

    private Status evaluateFromHighAlarm (double value, State state, AlarmConfig config, Instant now){

        if (value <= config.getHighAlarmClear()) {
            if (state.getPendingStatus() != Status.HIGH_WARNING){
                state.setPendingStatus(Status.HIGH_WARNING);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, config.getClearDelayMs())){

                return Status.HIGH_WARNING;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.HIGH_ALARM;
    }

    private Status evaluateFromLowAlarm (double value, State state, AlarmConfig config, Instant now){

        if (value >= config.getLowAlarmClear()) {
            if (state.getPendingStatus() != Status.LOW_WARNING){
                state.setPendingStatus(Status.LOW_WARNING);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, config.getClearDelayMs())){

                return Status.LOW_WARNING;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.LOW_ALARM;
    }

    private Status evaluateStatusTransition(double value, State state, AlarmConfig config, Instant now){

        switch (state.getStatus()){
            case OK:
                return this.determineRawStatus(value, config);
            case HIGH_WARNING:
                return this.evaluateFromHighWarning(value, state, config, now);
            case HIGH_ALARM:
                return this.evaluateFromHighAlarm(value, state, config, now);
            case LOW_WARNING:
                return this.evaluateFromLowWarning(value, state, config, now);
            case LOW_ALARM:
                return this.evaluateFromLowAlarm(value, state, config, now);
            default:
                throw new IllegalStateException("Unhandled status " + state.getStatus());
        }
    }

    public AlarmResult evaluate(MonitorReading<?> value) {


        AlarmConfig config = this.alarmConfigs.get(value.name());
        State state = this.monitorStates.get(value.name());

        AlarmResult event = null;

        Instant now = Instant.now();

        Status newStatus = this.evaluateStatusTransition((double)value.value(), state, config, now);

        if (newStatus != state.getStatus() ) {
            event = new AlarmResult(value.name(), (double)value.value(), newStatus, now);

            state.transitionTo(newStatus, now);
        }

        state.setValue((double)value.value());
        state.setLastEvaluated(now);

        return event;
    }
    
}
