package com.vigil.monitor;
import java.util.logging.Logger;

public abstract class Monitor {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    private final String name;

    public Monitor (String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public abstract double get();

}