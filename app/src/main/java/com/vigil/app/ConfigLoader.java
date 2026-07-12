package com.vigil.app;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException; // Required import


import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;
import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.ProcessCpuUsage;
import com.vigil.monitor.MemoryMonitor;
import com.vigil.monitor.SystemLoadAverage;
import com.vigil.monitor.JvmSystemMetricsProvider;
import com.vigil.monitor.SystemMetricsProvider;

public class ConfigLoader {

    private final TomlParseResult config;
    private final SystemMetricsProvider provider;
    
    public ConfigLoader(String configFile) throws IOException{
            InputStream configStream = new java.io.FileInputStream(configFile);
            this.config = Toml.parse(configStream);

            this.provider = new JvmSystemMetricsProvider();
    }

    public List<Monitor> buildMonitors(){

        TomlTable monitors = config.getTable("monitor");
        List<Monitor> monitorObj = new ArrayList<>();

        for (Map.Entry<String, Object> entry : monitors.entrySet()) {

            String monitorName = entry.getKey();

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

            String monitorName = entry.getKey();

            TomlTable alarmSetPoints = monitors.getTable(monitorName + ".alarm");

            double highWarning = alarmSetPoints.getDouble("highWarning");
            double highWarningClear = alarmSetPoints.getDouble("highWarningClear");

            double highAlarm = alarmSetPoints.getDouble("highAlarm");
            double highAlarmClear = alarmSetPoints.getDouble("highAlarmClear");

            double lowWarning = alarmSetPoints.getDouble("lowWarning");
            double lowWarningClear = alarmSetPoints.getDouble("lowWarningClear");

            double lowAlarm = alarmSetPoints.getDouble("lowAlarm");
            double lowAlarmClear = alarmSetPoints.getDouble("lowAlarmClear");

            long activationDelayMs = alarmSetPoints.getLong("activationDelayMs");
            long clearDelayMs = alarmSetPoints.getLong("clearDelayMs");

            alarmConfigs.put(monitorName, new AlarmConfig(monitorName,
                       highWarning,
                       highWarningClear,
                       highAlarm,
                       highAlarmClear,
                       lowWarning,
                       lowWarningClear,
                       lowAlarm,
                       lowAlarmClear,
                       activationDelayMs,
                       clearDelayMs));
        }

        return alarmConfigs;
     }

}
