package com.vigil.factory;

import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.MemoryMonitor;
import com.vigil.monitor.ProcessCpuUsage;
import com.vigil.monitor.SystemLoadAverage;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.monitor.Monitor;

public class MonitorFactory{

    public static Monitor<?> create(String monitorType, Double telemetryDeadband, SystemMetricsProvider provider){

        switch(monitorType){
            case "CPU":
                return new CpuMonitor(provider, telemetryDeadband);
                
            case "Memory":
                return new MemoryMonitor(provider, telemetryDeadband);
                
            case "ProcessCpuUsage":
                return new ProcessCpuUsage(provider, telemetryDeadband);
                
            case "SystemLoadAverage":
                return new SystemLoadAverage(provider, telemetryDeadband);
                
            default:
                throw new IllegalArgumentException("Unknown monitor type: " + monitorType);
        }
    }
}