package com.vigil.factory;

import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.MemoryMonitor;
import com.vigil.monitor.ProcessCpuUsage;
import com.vigil.monitor.SystemLoadAverage;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.monitor.Monitor;

public class MonitorFactory{

    public static Monitor create(String monitorType, SystemMetricsProvider provider){

        switch(monitorType){
            case "CPU":
                return new CpuMonitor(provider);
                
            case "Memory":
                return new MemoryMonitor(provider);
                
            case "ProcessCpuUsage":
                return new ProcessCpuUsage(provider);
                
            case "SystemLoadAverage":
                return new SystemLoadAverage(provider);
                
            default:
                throw new IllegalArgumentException("Unknown monitor type: " + monitorType);
        }
    }
}