package com.vigil.app;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.Map;

import com.vigil.exception.InvalidConfigurationException;
import com.vigil.config.ConfigValidator;

public class LoggerConfig {

    private static String logFile;
    private static int fileSizeMb;
    private static int fileCount;
    private static boolean logToConsole;

    private static void validate() {

        if (fileSizeMb < 0) {
            throw new InvalidConfigurationException( "Log File Size must be greater than 0!");
        }
        if (fileSizeMb > 1024) {
            throw new InvalidConfigurationException( "Log File Size must be less than 1024Mb!");
        }

        if (fileCount < 0) {
            throw new InvalidConfigurationException( "Log File Count must be greater than 0!");
        }

        if (fileCount > 1000) {
            throw new InvalidConfigurationException( "Log File Count must be less than 1000!");
        }
    }

     public static void fromMap(Map<String, Object> config) throws IOException {
        

        if (config == null) {
            throw new InvalidConfigurationException("Logger configuration is missing!");
        }

        logFile = ConfigValidator.requireString(config, "Logger", "fileName");
        fileSizeMb = (int)ConfigValidator.requireLong(config, "Logger", "fileSize");
        fileCount = (int)ConfigValidator.requireLong(config, "Logger", "fileCount");
        logToConsole = ConfigValidator.requireBool(config, "Logger", "toOSConsole");

        validate();

        Logger root = Logger.getLogger("");

        root.setUseParentHandlers(false);

        // Remove the default console handler(s)
        if (!logToConsole) {
            for (var handler : root.getHandlers()) {
                root.removeHandler(handler);
            }
        }

        FileHandler handler =
            new FileHandler(logFile, fileSizeMb * 1024 * 1024, fileCount, true);

        handler.setFormatter(new SimpleFormatter());

        root.addHandler(handler);
    }
}