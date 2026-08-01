package com.vigil.factory;

import java.util.Map;

import com.vigil.config.ConfigValidator;
import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.MemoryMonitor;
import com.vigil.monitor.ProcessCpuUsage;
import com.vigil.monitor.SystemLoadAverage;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmEvaluator;

public class MonitorFactory{

    public static Monitor<?> create(String type, Map<String, Object> monitorTable, SystemMetricsProvider provider){
        
        Map<String, Object> alarmTable = ConfigValidator.requireMap(monitorTable.get("alarm"), "Alarm Map");


        switch(type){
            case "CPU":{
                AlarmEvaluator<Double> evaluator = AlarmEvaluatorFactory.createNumeric(type, alarmTable);
                return new CpuMonitor(provider, evaluator, CpuMonitor.Configuration.fromMap(monitorTable));
            }
            case "Memory":{
                AlarmEvaluator<Double> evaluator = AlarmEvaluatorFactory.createNumeric(type, alarmTable);
                return new MemoryMonitor(provider, evaluator, MemoryMonitor.Configuration.fromMap(monitorTable));
            }
            case "ProcessCpuUsage":{
                AlarmEvaluator<Double> evaluator = AlarmEvaluatorFactory.createNumeric(type, alarmTable);
                return new ProcessCpuUsage(provider, evaluator, ProcessCpuUsage.Configuration.fromMap(monitorTable));
            }
            case "SystemLoadAverage":{
               AlarmEvaluator<Double> evaluator = AlarmEvaluatorFactory.createNumeric(type, alarmTable);
                return new SystemLoadAverage(provider, evaluator, SystemLoadAverage.Configuration.fromMap(monitorTable));
            }
            default:
                throw new IllegalArgumentException("Unknown monitor type: " + type);
        }
    }
}