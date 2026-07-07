package com.vigil.alarm;

import java.time.Instant;

public class State {

    private final String id;
    private Status status;
    private double value;
    private Instant stateEntered;

    
    public State (String id){
        this.id = id;
        this.stateEntered = Instant.now();
        this.value = 0.0;
        this.status = Status.OK;
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


    public void setStatus(Status s){
        this.status = s;
    }

    public void setValue(double v) {
        this.value = v;
    }

    public void setStateEntered(Instant ts){
        this.stateEntered = ts;
    }
}
