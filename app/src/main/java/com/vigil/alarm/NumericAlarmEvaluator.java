package com.vigil.alarm;

import com.vigil.monitor.MonitorReading;

import java.time.Instant;
import java.time.Duration;

public class NumericAlarmEvaluator extends AlarmEvaluator<Double> {

    private final NumericAlarmConfig config;

    public NumericAlarmEvaluator(NumericAlarmConfig config){

        this.config = config;
    }

    private boolean pendingTimeElapsed(
        State<Double> state,
        Instant now,
        long delayMs) {

        return Duration.between(
            state.getPendingTime(),
            now
        ).toMillis() >= delayMs;
    }
    
    public Status determineRawStatus(double val){

        Status status = Status.OK;

         if (val >= this.config.getHighAlarm()) {
            status = Status.HIGH_ALARM;
        } else if (val >= this.config.getHighWarning()) {
            status = Status.HIGH_WARNING;
        } else if (val <= this.config.getLowAlarm()) {
            status = Status.LOW_ALARM;
        } else if (val <= this.config.getLowWarning()) {
            status = Status.LOW_WARNING;
        }
        else {
            status = Status.OK;
        }

        return status;
    }

    private Status evaluateFromHighWarning(double value, State<Double> state, Instant now){

        //check if we need to move to OK status
        if (value >= this.config.getHighAlarm()) {
            if (state.getPendingStatus() != Status.HIGH_ALARM){
                state.setPendingStatus(Status.HIGH_ALARM);
                state.setPendingTime(now);
            }
            
            if (this.pendingTimeElapsed(state, now, config.getActivationDelayMs())){

                return Status.HIGH_ALARM;
            }
        } else if (value <= this.config.getHighWarningClear()) {
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

    private Status evaluateFromLowWarning(double value, State<Double> state, Instant now){

        //check if we need to move to OK status
        if (value <= this.config.getLowAlarm()) {
            if (state.getPendingStatus() != Status.LOW_ALARM){
                state.setPendingStatus(Status.LOW_ALARM);
                state.setPendingTime(now);
            }
            
            if (this.pendingTimeElapsed(state, now, config.getActivationDelayMs())){

                return Status.LOW_ALARM;
            }
        } else if (value >= this.config.getLowWarningClear()) {
            if (state.getPendingStatus() != Status.OK){
                state.setPendingStatus(Status.OK);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, this.config.getClearDelayMs())){

                return Status.OK;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.LOW_WARNING;
    }

    private Status evaluateFromHighAlarm (double value, State<Double> state, Instant now){

        if (value <= this.config.getHighAlarmClear()) {
            if (state.getPendingStatus() != Status.HIGH_WARNING){
                state.setPendingStatus(Status.HIGH_WARNING);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, this.config.getClearDelayMs())){

                return Status.HIGH_WARNING;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.HIGH_ALARM;
    }

    private Status evaluateFromLowAlarm (double value, State<Double> state, Instant now){

        if (value >= this.config.getLowAlarmClear()) {
            if (state.getPendingStatus() != Status.LOW_WARNING){
                state.setPendingStatus(Status.LOW_WARNING);
                state.setPendingTime(now);
            }

            if (this.pendingTimeElapsed(state, now, this.config.getClearDelayMs())){

                return Status.LOW_WARNING;
            }
        } else {
            state.clearPendingTransition();
        }

        return Status.LOW_ALARM;
    }

    private Status evaluateStatusTransition(double value, State<Double> state, NumericAlarmConfig config, Instant now){

        switch (state.getStatus()){
            case OK:
                return this.determineRawStatus(value);
            case HIGH_WARNING:
                return this.evaluateFromHighWarning(value, state, now);
            case HIGH_ALARM:
                return this.evaluateFromHighAlarm(value, state, now);
            case LOW_WARNING:
                return this.evaluateFromLowWarning(value, state, now);
            case LOW_ALARM:
                return this.evaluateFromLowAlarm(value, state, now);
            default:
                throw new IllegalStateException("Unhandled status " + state.getStatus());
        }
    }
    
    public Status evaluate(MonitorReading<Double> value, State<Double> currentState){

        Instant now = Instant.now();

        Status newStatus = this.evaluateStatusTransition((double)value.value(), currentState, config, now);

        return newStatus;
    }
}
