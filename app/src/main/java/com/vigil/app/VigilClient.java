package com.vigil.app;

import java.util.List;
import java.util.Map;

import com.vigil.monitor.JvmSystemMetricsProvider;
import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;
import com.vigil.config.ConfigLoader;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.config.TomlReader;
import com.vigil.dispatcher.Dispatcher;


public class VigilClient {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Usage: vigil <path/to/config.toml>");
            return;
        }

        try{

            SystemMetricsProvider provider = new JvmSystemMetricsProvider();

            //load the config file

            Map<String, Object> parseToml = new TomlReader(args[0]).getRoot();

            ConfigLoader loader = new ConfigLoader(parseToml, provider);

            //build a list of monitors
            List<Monitor> monitors = loader.buildMonitors();
            //build a Map of alarm configs
            Map<String, AlarmConfig> alarmConfigs = loader.buildAlarmConfigs();
            //build logger
            loader.buildLogger();

            List<Dispatcher> dispatchers = loader.buildDispatchers();

            VigilLoop mainLoop = new VigilLoop(monitors, dispatchers, alarmConfigs);

            mainLoop.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            //clean up logic here.
        }
    }
}
