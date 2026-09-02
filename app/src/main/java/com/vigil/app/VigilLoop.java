package com.vigil.app;

import java.util.List;
import java.util.logging.Logger;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmEngine;
import com.vigil.dispatcher.Dispatcher;
import com.vigil.dispatcher.OutgoingMessageQueue;
import com.vigil.telemetry.TelemetryTracker;
import com.vigil.listener.Listener;
import com.vigil.message.AlarmMessage;
import com.vigil.message.TelemetryOut;
import com.vigil.message.VigilMessage;

public class VigilLoop {

    private static final Logger logger = Logger.getLogger(VigilLoop.class.getName());

    private final AlarmEngine alarmEngine;
    private final AppConfig appConfig;
    private final List<Dispatcher> dispatchers;
    private final List<Monitor<?>> monitors;
    private final List<Listener> listeners;
    private final TelemetryTracker telemetry;
    private final OutgoingMessageQueue outputMessageQueue;
    private volatile boolean runLoop = true;
    
    private Thread outputThread;


    public VigilLoop(AppConfig appConfig,
            List<Monitor<?>> monitors,
            List<Dispatcher> dispatchers,
            List<Listener> listeners,
            AlarmEngine alarmEngine,
            OutgoingMessageQueue outputMessageQueue){

        this.alarmEngine = alarmEngine;
        this.appConfig = appConfig;
        this.dispatchers = dispatchers;
        this.monitors = monitors;
        this.listeners = listeners;
        this.outputMessageQueue = outputMessageQueue;

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

        this.processOutputQueue();

        while(runLoop){
            for (Monitor<?> m : this.monitors){
                try{
                    processMonitor(m);
                } catch (Exception e) {
                    logger.severe(e + "in program loop");
                }
            }

            this.sleep(appConfig.getPollingIntervalMs());
        }
    }

    private <T> void processMonitor(Monitor<T> monitor) {
        TelemetryOut<T> value = monitor.read();
        AlarmMessage<T> result = this.alarmEngine.evaluate(value, monitor.getAlarmEvaluator());
        Boolean sendTelemetry = this.telemetry.shouldDispatch(value);

        if (sendTelemetry) this.outputMessageQueue.submit(value);
        if (result != null) this.outputMessageQueue.submit(result);

    }


    private void processOutputQueue() {

        this.outputThread = new Thread(() -> {

            while (runLoop) {

                try {
                    VigilMessage output =
                        this.outputMessageQueue.take();

                    for (Dispatcher d : this.dispatchers){
                        d.send(output);
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

        }, "vigil-output");

        this.outputThread.start();
    }

    public void stop(){
        logger.info("Stopping Vigil.");
        this.runLoop = false;

                // Wake the acknowledgement thread if it is blocked in take()
        if (outputThread != null) {
            outputThread.interrupt();
        }

        for (Listener l : this.listeners){
            l.stop();
        }

        for (Dispatcher d : this.dispatchers){
            d.stop();
        }
    }
}
