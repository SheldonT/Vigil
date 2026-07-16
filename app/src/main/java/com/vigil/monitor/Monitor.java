package com.vigil.monitor;


public abstract class Monitor {

    private final String name;

    public Monitor (String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public abstract double get();

}