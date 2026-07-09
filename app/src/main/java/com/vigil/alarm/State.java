package com.vigil.alarm;

import java.time.Instant;

public class State {

    private final String id;
    private Status status;
    private Status pendingStatus;
    private double value;
    private Instant stateEntered;
    private Instant lastEvaluated;
    private Instant pendingSince;

    
    public State (String id){
        this.id = id;
        this.stateEntered = Instant.now();
        this.pendingSince = Instant.now();
        this.value = 0.0;
        this.status = Status.OK;
        this.pendingStatus = Status.OK;
    }

    public String getId(){
        return this.id;
    }
    
    public Status getStatus(){
        return this.status;
    }

    public double getValue() {
        return this.value;
    }

    public Instant getStateEntered(){
        return this.stateEntered;
    }

    public Instant getLastEvaluated(){
        return this.lastEvaluated;
    }
    public Instant getPendingTime(){
        return this.pendingSince;
    }

    public Status getPendingStatus(){
        return this.pendingStatus;
    }

    public void setState(Status s, double v, Instant ts){
        this.status = s;
        this.value = v;
        this.stateEntered = ts;
    }

    public void transitionTo(Status s, Instant now){
        this.status = s;
        this.stateEntered = now;
        this.clearPendingTransition();
    }

    public void setValue(double v) {
        this.value = v;
    }
    
    public void setStateEntered(Instant ts){
        this.stateEntered = ts;
    }

    public void setLastEvaluated(Instant ts){
        this.lastEvaluated = ts;
    }
    public void setPendingTime(Instant ts){
        this.pendingSince = ts;
    }

    public void setPendingStatus(Status pendingStatus){
        this.pendingStatus = pendingStatus;
    }

    public void clearPendingTransition(){
        this.pendingStatus = null;
        this.pendingSince = null;
    }
}
