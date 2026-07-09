package com.vigil.app;

import java.util.List;
import java.util.Map;
import java.util.logging.*;

import com.vigil.monitor.Monitor;
import com.vigil.alarm.AlarmConfig;
import com.vigil.alarm.AlarmEngine;
import com.vigil.alarm.AlarmResult;

public class VigilLoop {
    
    private boolean runLoop = true;

    private final Logger logger;

    private final AlarmEngine alarmEngine;

    public VigilLoop(List<Monitor> monitors, Map<String, AlarmConfig> alarmConfigs){

        this.alarmEngine = new AlarmEngine(monitors, alarmConfigs);

        this.logger = Logger.getLogger("Vigil");

        try {
            FileHandler fileHandler = new FileHandler(
                "vigil.log",
                1024 * 1024,  // 1MB per file
                5             // keep 5 rotated logs
            );

            fileHandler.setFormatter(new SimpleFormatter());
            this.logger.addHandler(fileHandler);
            this.logger.setUseParentHandlers(false);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void sleep (int ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception e) {
            this.logger.warning(e + "while sleeping");
        }
    }

    public void start(){

        try {
            System.out.println("Starting Vigil...");

            //AlarmEngine alarmEngine = new AlarmEngine(monitors, alarmConfigs);

            while(runLoop){

                List<AlarmResult> events = alarmEngine.evaluate();

                for(AlarmResult result : events){
                    this.logger.info(
                        result.timestampNow + " | " + result.name + " " + result.status + " : " + result.value
                    );
                }
                
                this.sleep(500);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stop(){
        System.out.println("Stopping Vigil.");
        this.runLoop = false;
    }
}
