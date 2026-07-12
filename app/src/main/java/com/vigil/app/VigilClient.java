package com.vigil.app;

import java.util.List;
import java.util.Map;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;


public class VigilClient {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Usage: vigil <path/to/config.toml>");
            return;
        }

        try{
            //load the config file
            ConfigLoader loader = new ConfigLoader(args[0]);

            //build a list of monitors
            List<Monitor> monitors = loader.buildMonitors();
            //build a Map of alarm configs
            Map<String, AlarmConfig> alarmConfigs = loader.buildAlarmConfigs();

            VigilLoop mainLoop = new VigilLoop(monitors, alarmConfigs);

            mainLoop.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            //clean up logic here.
        }
    }
}
