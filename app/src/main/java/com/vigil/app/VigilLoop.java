package com.vigil.app;

import java.util.List;
import java.util.logging.Logger;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmEngine;
import com.vigil.alarm.AlarmResult;
import com.vigil.dispatcher.Dispatcher;
import com.vigil.monitor.MonitorReading;
import com.vigil.telemetry.TelemetryTracker;
import com.vigil.alarm.AlarmMessage;

public class VigilLoop {
    
    private boolean runLoop = true;

    private static final Logger logger = Logger.getLogger(VigilLoop.class.getName());

    private final AlarmEngine alarmEngine;
    private final List<Dispatcher> dispatchers;
    private final List<Monitor<?>> monitors;
    private final TelemetryTracker telemetry;

    public VigilLoop(List<Monitor<?>> monitors, List<Dispatcher> dispatchers){

        this.alarmEngine = new AlarmEngine(monitors);
        this.dispatchers = dispatchers;
        this.monitors = monitors;

        this.telemetry = new TelemetryTracker(monitors);
    }

    private void sleep (int ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception e) {
            logger.warning(e + "while sleeping in program loop");
        }
    }

    public void start(){
        
        logger.info("Starting Vigil...");

        while(runLoop){
            for (Monitor<?> m : this.monitors){
                try{
                    processMonitor(m);
                } catch (Exception e) {
                    logger.severe(e + "in program loop");
                }
            }
            this.sleep(500);
        }
    }

    private <T> void processMonitor(Monitor<T> monitor) {
        MonitorReading<T> value = monitor.read();
        AlarmMessage<T> result = this.alarmEngine.evaluate(value, monitor.getAlarmEvaluator());
        Boolean sendTelemetry = this.telemetry.shouldDispatch(value);

        for (Dispatcher d : this.dispatchers){
            if (sendTelemetry) d.sendValue(value);
            if (result != null) d.sendAlarm(result);
        }
    }

    public void stop(){
        logger.info("Stopping Vigil.");
        this.runLoop = false;
    }
}
