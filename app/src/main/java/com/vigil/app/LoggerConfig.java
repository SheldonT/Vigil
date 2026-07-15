package com.vigil.app;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.Map;

import com.vigil.exception.InvalidConfigurationException;
import org.tomlj.TomlTable;

public class LoggerConfig {

    private static String logFile;
    private static int fileSizeMb;
    private static int fileCount;
    private static boolean logToConsole;

    private static String requireString(TomlTable table, String key) {
        String value = table.getString(key);

        if (value == null) {
            throw new IllegalArgumentException("Missing required log file name (" + key + ")!");
        }
        return value;
    }

    private static int requireLong(TomlTable table, String key) {
        Long value = table.getLong(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required log file size (" + key + ")!");
        }
        return value.intValue();
    }

    private static boolean requireBool(TomlTable table, String key) {
        Boolean value = table.getBoolean(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required log file count (" + key + ")!");
        }
        return value;
    }

    private static String mapRequireString(Map<String, Object> table, String key) {
        String value = (String)table.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Missing required log file name (" + key + ")!");
        }
        return value;
    }

    private static int mapRequireLong(Map<String, Object> table, String key) {
        Long value = (Long)table.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required log file size (" + key + ")!");
        }
        return value.intValue();
    }

    private static boolean mapRequireBool(Map<String, Object> table, String key) {
        Boolean value = (Boolean)table.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required log file count (" + key + ")!");
        }
        return value;
    }

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

    public static void fromToml(TomlTable config) throws IOException {


        if (config == null) {
            throw new InvalidConfigurationException("Logger configuration is missing!");
        }

        logFile = requireString(config, "fileName");
        fileSizeMb = requireLong(config, "fileSize");
        fileCount = requireLong(config, "fileCount");
        logToConsole = requireBool(config, "toOSConsole");

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

     public static void fromMap(Map<String, Object> config) throws IOException {
        

        if (config == null) {
            throw new InvalidConfigurationException("Logger configuration is missing!");
        }

        logFile = mapRequireString(config, "fileName");
        fileSizeMb = mapRequireLong(config, "fileSize");
        fileCount = mapRequireLong(config, "fileCount");
        logToConsole = mapRequireBool(config, "toOSConsole");

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