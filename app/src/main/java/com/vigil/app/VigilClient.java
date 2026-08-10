package com.vigil.app;

import java.util.List;
import java.util.Map;

import com.vigil.monitor.JvmSystemMetricsProvider;
import com.vigil.monitor.Monitor;
import com.vigil.config.ConfigLoader;
import com.vigil.monitor.SystemMetricsProvider;
import com.vigil.config.TomlReader;
import com.vigil.dispatcher.Dispatcher;
import com.vigil.alarm.AlarmEngine;
import com.vigil.listener.Listener;

public class VigilClient {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Usage: vigil <path/to/config.toml>");
            return;
        }

        ShutdownHandler shutdownHandler = new ShutdownHandler();

        VigilLoop mainLoop = null;

        try{

            SystemMetricsProvider provider = new JvmSystemMetricsProvider();

            //load the config file
            Map<String, Object> parseToml = new TomlReader(args[0]).getRoot();

            ConfigLoader loader = new ConfigLoader(parseToml, provider);

            //build logger
            loader.buildLogger();

            //build a list of monitors
            List<Monitor<?>> monitors = loader.buildMonitors();

            //create the alarm engine
            AlarmEngine alarmEngine = new AlarmEngine(monitors);

            //build a list of listeners (they depend on the alarm engine)
            List<Listener> listeners = loader.buildListeners(alarmEngine);

            //build a list of dispatchers
            List<Dispatcher> dispatchers = loader.buildDispatchers();

            mainLoop = new VigilLoop(monitors, dispatchers, listeners, alarmEngine);

            shutdownHandler.register(mainLoop::stop);

            mainLoop.start();

        } catch (Exception e) {
            //e.printStackTrace();
            System.out.print(e);
        }
        finally {
            if (mainLoop != null){
                mainLoop.stop();
            }
        }
    }
}
