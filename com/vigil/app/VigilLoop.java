package com.vigil.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.MonitorResult;
import com.vigil.monitor.Status;

public class VigilLoop {
    
    private boolean runLoop = true;
    private final List<Monitor> monitors;

    private final Logger logger;

    public VigilLoop(List<Monitor> monitors){
        this.monitors = monitors;

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

    private void sleep (int seconds) {
        try {
            int s = seconds * 1000;
            Thread.sleep(s);
        } catch(Exception e) {
            this.logger.warning(e + "while sleeping");
        }
    }

    public void start(){

        try {
            System.out.println("Starting Vigil...");

            Map<String, Status> lastStatusMap = new HashMap<>();

            while(runLoop){
                
                for (Monitor m : monitors){

                    MonitorResult result = m.check();

                    Status last = lastStatusMap.getOrDefault(result.name, Status.OK);


                    if (result.status != last) {
                        this.logger.info(
                            result.name + " changed: " + last + " → " + result.status + " : " + result.value
                        );

                        lastStatusMap.put(result.name, result.status);
                    }
                }
                
                this.sleep(1);
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
