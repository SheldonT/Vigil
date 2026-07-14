package com.vigil.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;
import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.ProcessCpuUsage;
import com.vigil.monitor.MemoryMonitor;
import com.vigil.monitor.SystemLoadAverage;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.app.LoggerConfig;

public class ConfigLoader {

    private final TomlParseResult config;
    private final SystemMetricsProvider provider;
    
    public ConfigLoader(String configFile, SystemMetricsProvider provider) throws IOException{
            InputStream configStream = new java.io.FileInputStream(configFile);
            this.config = Toml.parse(configStream);

            this.provider = provider;
    }

    public List<Monitor> buildMonitors(){

        TomlTable monitors = config.getTable("monitor");
        List<Monitor> monitorObj = new ArrayList<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            TomlTable monitorTable = (TomlTable)entry.getValue();

            String monitorName = monitorTable.getString("type");

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

        TomlTable monitors = config.getTable("monitor");
        Map<String, AlarmConfig> alarmConfigs = new HashMap<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            TomlTable monitorTable = (TomlTable)entry.getValue();
            String monitorName = monitorTable.getString("type");
            TomlTable alarmTable = monitorTable.getTable("alarm");

            alarmConfigs.put(monitorName, AlarmConfig.fromToml(monitorName, alarmTable));
        }

        return alarmConfigs;
     }

     public void buildLogger() throws IOException{

        TomlTable loggerConfig = config.getTable("logging");

        LoggerConfig.fromToml(loggerConfig);
     }
}
