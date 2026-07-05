package com.vigil.app;

import java.util.List;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.CpuMonitor;
import com.vigil.monitor.JvmSystemMetricsProvider;
import com.vigil.monitor.SystemMetricsProvider;

public class VigilClient {

    public static void main(String[] args) {

        SystemMetricsProvider provider = new JvmSystemMetricsProvider();

        List<Monitor> monitors = List.of(
            new CpuMonitor(provider, 85.0, 90.0, 0.0, 0.0)
        );

        VigilLoop mainLoop = new VigilLoop(monitors);

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
