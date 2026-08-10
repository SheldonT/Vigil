package com.vigil.alarm;

import java.time.Instant;

public class MonitorState<T> {

    private final String id;
    private Status status;
    private Status pendingStatus;
    private T value;
    private Instant stateEntered;
    private Instant lastEvaluated;
    private Instant pendingSince;

    
    public MonitorState (String id){
        this.id = id;
        this.stateEntered = Instant.now();
        this.pendingSince = Instant.now();
        this.status = Status.OK;
        this.pendingStatus = Status.OK;
    }

    public String getId(){
        return this.id;
    }
    
    public Status getStatus(){
        return this.status;
    }

    public T getValue() {
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

    public void setState(Status s, T v, Instant ts){
        this.status = s;
        this.value = v;
        this.stateEntered = ts;
    }

    public void transitionTo(Status s, Instant now){
        this.status = s;
        this.stateEntered = now;
        this.clearPendingTransition();
    }

    public void setValue(T v) {
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
