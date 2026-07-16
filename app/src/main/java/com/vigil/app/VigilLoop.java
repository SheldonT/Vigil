package com.vigil.app;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;
import com.vigil.alarm.AlarmEngine;
import com.vigil.alarm.AlarmResult;
import com.vigil.dispatcher.Dispatcher;

public class VigilLoop {
    
    private boolean runLoop = true;

    private static final Logger logger = Logger.getLogger(VigilLoop.class.getName());

    private final AlarmEngine alarmEngine;
    private final List<Dispatcher> dispatchers;

    public VigilLoop(List<Monitor> monitors, List<Dispatcher> dispatchers, Map<String, AlarmConfig> alarmConfigs){

        this.alarmEngine = new AlarmEngine(monitors, alarmConfigs);
        this.dispatchers = dispatchers;
    }

    private void sleep (int ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception e) {
            logger.warning(e + "while sleeping");
        }
    }

    public void start(){
        
        System.out.println("Starting Vigil...");


        while(runLoop){
            try {
                List<AlarmResult> events = alarmEngine.evaluate();

                for(AlarmResult result : events){

                    for(Dispatcher dispatch : this.dispatchers){

                        dispatch.send(result);
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
            }

            this.sleep(500);
        }
    }

    public void stop(){
        System.out.println("Stopping Vigil.");
        this.runLoop = false;
    }
}
