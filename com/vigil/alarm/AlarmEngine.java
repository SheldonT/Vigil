package com.vigil.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.Instant;

import com.vigil.monitor.Monitor;


public class AlarmEngine {

    private final Map<String, AlarmConfig> alarmConfigs;
    private final List<Monitor> monitors;

    private Map<String, AlarmResult> lastStatusMap = new HashMap<>();

    public AlarmEngine(List<Monitor> monitors, Map<String, AlarmConfig> alarmConfigs) {
        this.monitors = monitors;
        this.alarmConfigs = alarmConfigs;

        for (Monitor m : monitors){
            if (!alarmConfigs.containsKey(m.getName())){
                throw new IllegalStateException("Missing alarm configs for " + m.getName());
            }
        }

    }

    public List<AlarmResult> evaluate (){

        List<AlarmResult> newStatus = new ArrayList<>();

        for (Monitor m : monitors){

            AlarmConfig config = this.alarmConfigs.get(m.getName());
            
            Status status;

            double value = m.get();

            if (value >= config.getHighAlarm()) {
                status = Status.HIGH_ALARM;
            } else if (value >= config.getHighWarning()) {
                status = Status.HIGH_WARNING;
            }
            // else if (m.get() <= config.getLowWarning()) {
            //     status = Status.LOW_WARNING;
            // }
            // else if (m.get() <= config.getLowAlarm()) {
            //     status = Status.LOW_ALARM;
            // }
            else {
                status = Status.OK;
            }
                // AlarmResult result = m.check();
            
            AlarmResult newState = new AlarmResult(
                m.getName(), value, status, Instant.now()
            );

            AlarmResult lastStatus = lastStatusMap.getOrDefault(m.getName(), newState);


            if (status != lastStatus.status) {

                newStatus.add(newState); //return the current state, because it changed
            }

            lastStatusMap.put(m.getName(), newState);
        }

        return newStatus;
    }
    
}
