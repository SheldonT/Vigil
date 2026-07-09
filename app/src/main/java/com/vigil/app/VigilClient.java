package com.vigil.app;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.JvmSystemMetricsProvider;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.alarm.AlarmConfig;

public class VigilClient {

    public static void main(String[] args) {

        SystemMetricsProvider provider = new JvmSystemMetricsProvider();

        List<Monitor> monitors = List.of(
            new CpuMonitor(provider)
        );

        Map<String, AlarmConfig> alarmConfigs = new HashMap<>();

        alarmConfigs.put("CPU", new AlarmConfig("CPU",
                       85.0,
                       84.0,
                       90.0,
                       89.0,
                       5,
                       6,
                       2,
                       1,
                       3000,
                       3000)
        );

        VigilLoop mainLoop = new VigilLoop(monitors, alarmConfigs);

        try {
            mainLoop.start();

        } catch (Exception e) {
            System.out.println(e);
        }
        finally {
            //clean up logic here.
        }
    }
}
