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
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.factory.MonitorFactory;
import com.vigil.factory.DispatcherFactory;
import com.vigil.dispatcher.Dispatcher;

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
            String monitorType = (String)monitorTable.get("type");

            monitorObj.add(MonitorFactory.create(monitorType, provider));
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

     public List<Dispatcher> buildDispatchers(){
            Map<String, Object> dispatchers = requireMap(config.get("dispatcher"), "Dispatcher Config Map");
            List<Dispatcher> dispatcherObj = new ArrayList<>();

            for (Map.Entry<String, Object> entry : dispatchers.entrySet()) {
                Map<String, Object> dispatcher = requireMap(entry.getValue(), "Dispatcher Settings");

                dispatcherObj.add(DispatcherFactory.create(dispatcher));
            }

            return dispatcherObj;
     }

     public void buildLogger() throws IOException{

        Map<String, Object> loggerConfig = requireMap(config.get("logging"), "Logging Settings");

        LoggerConfig.fromMap(loggerConfig);
     }
}
