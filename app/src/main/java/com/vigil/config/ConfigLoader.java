package com.vigil.config;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;
import com.vigil.app.LoggerConfig;
import com.vigil.exception.InvalidConfigurationException;
import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.ProcessCpuUsage;
import com.vigil.monitor.MemoryMonitor;
import com.vigil.monitor.SystemLoadAverage;
import com.vigil.monitor.SystemMetricsProvider;

public class ConfigLoader {

    private final Map<String, Object> config;
    private final SystemMetricsProvider provider;
    
    public ConfigLoader(Map<String, Object> config, SystemMetricsProvider provider) throws IOException{

            this.config = config;

            this.provider = provider;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> requireMap(
            Object value,
            String description) {

        if (!(value instanceof Map<?, ?> map)) {
            throw new InvalidConfigurationException(
                description + " must be a table."
            );
        }

        return (Map<String, Object>) map;
    }

    public List<Monitor> buildMonitors(){

        Map<String, Object> monitors = requireMap(this.config.get("monitor"), "Monitors Map");

        List<Monitor> monitorObj = new ArrayList<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            Map<String, Object> monitorTable = requireMap(entry.getValue(), "Single Monitor Map");

            String monitorName = (String) monitorTable.get("type");

            switch(monitorName){
                case "CPU":
                    monitorObj.add(new CpuMonitor(this.provider));
                    break;
                case "Memory":
                    monitorObj.add(new MemoryMonitor(this.provider));
                    break;
                case "ProcessCpuUsage":
                    monitorObj.add(new ProcessCpuUsage(this.provider));
                    break;
                case "SystemLoadAverage":
                    monitorObj.add(new SystemLoadAverage(this.provider));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown monitor type: " + monitorName);
            }
        }

        return monitorObj;
     }

    public Map<String, AlarmConfig> buildAlarmConfigs(){

        Map<String, Object> monitors = requireMap(config.get("monitor"), "Alarm Config Map");
        Map<String, AlarmConfig> alarmConfigs = new HashMap<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            Map<String, Object> monitorTable = requireMap(entry.getValue(), "Monitor Table");

            String monitorName = (String) monitorTable.get("type");
            Map<String, Object> alarmTable = requireMap(monitorTable.get("alarm"), "Alarm Map");

            
            alarmConfigs.put(monitorName, AlarmConfig.fromMap(monitorName, alarmTable));
        }

        return alarmConfigs;
     }

     public void buildLogger() throws IOException{

        Map<String, Object> loggerConfig = requireMap(config.get("logging"), "Logging Settings");

        LoggerConfig.fromMap(loggerConfig);
     }
}
