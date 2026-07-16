package com.vigil.app;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ShutdownHandler {

    private static final Logger logger = Logger.getLogger(ShutdownHandler.class.getName());

    private final List<Runnable> tasks = new ArrayList<>();

    public ShutdownHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::runAll));
    }

    public void register(Runnable task) {
        tasks.add(task);
    }

    private void runAll() {
        logger.info("Running shutdown tasks...");
        for (Runnable task : tasks) {
            try {
                task.run();
            } catch (Exception e) {
                logger.severe("Error during shutdown: " + e);
            }
        }
    }
}
