package com.vigil.app;

import java.util.List;
import java.util.logging.Logger;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmEngine;
import com.vigil.dispatcher.Dispatcher;
import com.vigil.monitor.TelemetryOut;
import com.vigil.telemetry.TelemetryTracker;
import com.vigil.alarm.AlarmMessage;
import com.vigil.listener.Listener;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.AlarmAcknowledgeOut;

public class VigilLoop {

    private static final Logger logger = Logger.getLogger(VigilLoop.class.getName());

    private final AlarmEngine alarmEngine;
    private final AppConfig appConfig;
    private final List<Dispatcher> dispatchers;
    private final List<Monitor<?>> monitors;
    private final List<Listener> listeners;
    private final TelemetryTracker telemetry;
    
    private volatile boolean runLoop = true;
    private Thread acknowledgementThread;


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

        this.startAcknowledgementThread();

        while(runLoop){
            for (Monitor<?> m : this.monitors){
                try{
                    processMonitor(m);
                } catch (Exception e) {
                    logger.severe(e + "in program loop");
                }
            }

            this.processAlarmAcknowledgeFailures();

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

    private void startAcknowledgementThread() {

        this.acknowledgementThread = new Thread(() -> {

            while (runLoop) {

                try {
                    AlarmAcknowledgeOut acknowledgement =
                        this.alarmEngine.getAckQueue().take();

                    for (Dispatcher dispatcher : dispatchers) {
                        dispatcher.sendAlarmAcknowledgement(acknowledgement);
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;

                } catch (Exception e) {

                    logger.severe(
                        "Error processing alarm acknowledgement: " + e
                    );
                }
            }

        }, "vigil-acknowledgement");

        this.acknowledgementThread.start();
    }

    private void processAlarmAcknowledgeFailures() {
        while (true) {
            AlarmAcknowledgeFail failure = this.alarmEngine.pollAcknowledgeFail();
            if (failure == null) {
                break;
            }

            for (Dispatcher dispatcher : this.dispatchers) {
                dispatcher.sendAlarmAcknowledgeFail(failure);
            }
        }
    }

    public void stop(){
        logger.info("Stopping Vigil.");
        this.runLoop = false;

                // Wake the acknowledgement thread if it is blocked in take()
        if (acknowledgementThread != null) {
            acknowledgementThread.interrupt();
        }

        for (Listener l : this.listeners){
            l.stop();
        }

        for (Dispatcher d : this.dispatchers){
            d.stop();
        }
    }
}
