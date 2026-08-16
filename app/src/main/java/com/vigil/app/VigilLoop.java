package com.vigil.app;

import java.util.List;
import java.util.logging.Logger;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmAcknowledgeOut;
import com.vigil.alarm.AlarmEngine;
import com.vigil.dispatcher.Dispatcher;
import com.vigil.monitor.TelemetryOut;
import com.vigil.telemetry.TelemetryTracker;
import com.vigil.alarm.AlarmMessage;
import com.vigil.listener.Listener;

public class VigilLoop {
    
    private boolean runLoop = true;

    private static final Logger logger = Logger.getLogger(VigilLoop.class.getName());

    private final AlarmEngine alarmEngine;
    private final AppConfig appConfig;
    private final List<Dispatcher> dispatchers;
    private final List<Monitor<?>> monitors;
    private final List<Listener> listeners;
    private final TelemetryTracker telemetry;


    public VigilLoop(AppConfig appConfig, List<Monitor<?>> monitors, List<Dispatcher> dispatchers, List<Listener> listeners, AlarmEngine alarmEngine){

        this.alarmEngine = alarmEngine;
        this.appConfig = appConfig;
        this.dispatchers = dispatchers;
        this.monitors = monitors;
        this.listeners = listeners;

        this.telemetry = new TelemetryTracker(monitors);
    }

    private void sleep (long ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception e) {
            logger.warning(e + "while sleeping in program loop");
        }
    }

    public void start(){
        
        logger.info("Starting Vigil...");

        for (Listener l : this.listeners){
            l.start();
        }

        for (Dispatcher d : this.dispatchers){
            d.start();
        }

        this.dispatchStartupAlarms();

        while(runLoop){
            for (Monitor<?> m : this.monitors){
                try{
                    processMonitor(m);
                } catch (Exception e) {
                    logger.severe(e + "in program loop");
                }
            }

            this.processAlarmAcknowledge();

            this.sleep(appConfig.getPollingIntervalMs());
        }
    }

    private <T> void processMonitor(Monitor<T> monitor) {
        TelemetryOut<T> value = monitor.read();
        AlarmMessage<T> result = this.alarmEngine.evaluate(value, monitor.getAlarmEvaluator());
        Boolean sendTelemetry = this.telemetry.shouldDispatch(value);

        for (Dispatcher d : this.dispatchers){
            if (sendTelemetry) d.sendValue(value);
            if (result != null) d.sendAlarm(result);
        }
    }

    private void dispatchStartupAlarms() {
        AlarmMessage<?> alarm;
        while ((alarm = this.alarmEngine.pollStartupAlarm()) != null) {
            for (Dispatcher dispatcher : this.dispatchers) {
                dispatcher.sendAlarm(alarm);
            }
        }
    }

    private void processAlarmAcknowledge(){

        while (true){

            AlarmAcknowledgeOut acknowledgement = this.alarmEngine.getAckQueue().poll();
            
            if (acknowledgement == null) {
                break;
            }

            for (Dispatcher dispatcher : dispatchers) {
                
                dispatcher.sendAlarmAcknowledgement(acknowledgement);
            }
        }
    }

    public void stop(){
        logger.info("Stopping Vigil.");
        this.runLoop = false;
    }
}
