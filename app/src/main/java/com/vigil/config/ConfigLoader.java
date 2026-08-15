package com.vigil.config;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.logging.Logger;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.NumericAlarmConfig;
import com.vigil.app.LoggerConfig;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.factory.MonitorFactory;
import com.vigil.factory.DispatcherFactory;
import com.vigil.factory.ListenerFactory;
import com.vigil.listener.Listener;
import com.vigil.dispatcher.Dispatcher;
import com.vigil.alarm.AlarmEngine;
import com.vigil.app.AppConfig;

public class ConfigLoader {

    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());

    private final Map<String, Object> config;
    private final SystemMetricsProvider provider;
    
    public ConfigLoader(Map<String, Object> config, SystemMetricsProvider provider) throws IOException{

            this.config = config;

            this.provider = provider;
    }

    public AppConfig buildAppConfig() {
         Map<String, Object> appConfigMap = ConfigValidator.requireMap(this.config.get("app"), "App Config");

         return AppConfig.fromMap("app", appConfigMap);
    }

    public List<Monitor<?>> buildMonitors(){

        logger.info("Building monitor list...");

        Map<String, Object> monitors = ConfigValidator.requireMap(this.config.get("monitor"), "Monitors Map");

        List<Monitor<?>> monitorObj = new ArrayList<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            Map<String, Object> monitorTable = ConfigValidator.requireMap(entry.getValue(), "Single Monitor Map");

            String monitorType = ConfigValidator.requireString(monitorTable, entry.getKey(), "type");
            // Double telemetryDeadband = ConfigValidator.requireDouble(monitorTable, entry.getKey(), "telemetryDeadband");

            logger.info("Adding " + monitorType + " monitor to the list");

            monitorObj.add(MonitorFactory.create(monitorType, monitorTable, provider));
        }
        logger.info(monitorObj.size() + " configured.");
        return monitorObj;
     }

    public Map<String, NumericAlarmConfig> buildAlarmConfigs(){

        logger.info("Building monitor config map...");

        Map<String, Object> monitors = ConfigValidator.requireMap(config.get("monitor"), "Alarm Config Map");
        Map<String, NumericAlarmConfig> alarmConfigs = new HashMap<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            Map<String, Object> monitorTable = ConfigValidator.requireMap(entry.getValue(), "Monitor Table");

            String monitorName = (String) monitorTable.get("type");
            Map<String, Object> alarmTable = ConfigValidator.requireMap(monitorTable.get("alarm"), "Alarm Map");

            logger.info("Adding config for monitor" + monitorName + " to config map");

            alarmConfigs.put(monitorName, NumericAlarmConfig.fromMap(monitorName, alarmTable));
        }

        logger.info(alarmConfigs.size() + " monitor configs created");

        return alarmConfigs;
     }

     public List<Dispatcher> buildDispatchers(){

            logger.info("Building dispatcher list...");
            Map<String, Object> dispatchers = ConfigValidator.requireMap(config.get("dispatcher"), "Dispatcher Config Map");
            List<Dispatcher> dispatcherObj = new ArrayList<>();

            for (Map.Entry<String, Object> entry : dispatchers.entrySet()) {
                Map<String, Object> dispatcher = ConfigValidator.requireMap(entry.getValue(), "Dispatcher Settings");
                logger.info("Adding " + entry.getKey() + " to dispatcher list");
                dispatcherObj.add(DispatcherFactory.create(dispatcher));
            }

            logger.info(dispatcherObj.size() + " dispatchers created");

            return dispatcherObj;
     }

    public List<Listener> buildListeners(AlarmEngine alarmEngine){

        logger.info("Building listener list...");
        Map<String, Object> listeners = ConfigValidator.requireMap(config.get("listener"), "Dispatcher Config Map");
        List<Listener> listenerObj = new ArrayList<>();

        for (Map.Entry<String, Object> entry : listeners.entrySet()) {
            Map<String, Object> listener = ConfigValidator.requireMap(entry.getValue(), "listener Settings");
            logger.info("Adding " + entry.getKey() + " to listener list");
            listenerObj.add(ListenerFactory.create(listener, alarmEngine));
        }

        logger.info(listenerObj.size() + " dispatchers created");

        return listenerObj;
     }

     public void buildLogger() throws IOException{

        logger.info("Initializing logger...");

        Map<String, Object> loggerConfig = ConfigValidator.requireMap(config.get("logging"), "Logging Settings");

        LoggerConfig.fromMap(loggerConfig);

        logger.info("Logger initialized");
     }
}
