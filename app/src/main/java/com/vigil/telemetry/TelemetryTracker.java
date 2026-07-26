package com.vigil.telemetry;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.MonitorReading;

public class TelemetryTracker {

    private Map<String, MonitorReading<?>> lastReadings = new HashMap<>();
    private Map<String, Double> telemetryDeadbands = new HashMap<>();
    
    public TelemetryTracker(List<Monitor<?>> monitorList){
        this.lastReadings = initLastValue(monitorList);
    }

    private Map<String, MonitorReading<?>> initLastValue(List<Monitor<?>> monitorList){
        Map<String, MonitorReading<?>> initialReadings = new HashMap<>();

        for (Monitor<?> m : monitorList){
            initialReadings.put(m.getName(), null);
            telemetryDeadbands.put(m.getName(), m.getTelemetryDeadband());
        }

        return initialReadings;
    }

    public Boolean shouldDispatch(MonitorReading<?> current){

        MonitorReading<?> previous = this.lastReadings.get(current.name());
        Double telemetryDeadband = this.telemetryDeadbands.get(current.name());

        if ( previous == null || Math.abs((double)previous.value() - (double)current.value()) > telemetryDeadband){
            this.lastReadings.put(current.name(), current);
            return true;
        } else {
            return false;
        }
    }
}
