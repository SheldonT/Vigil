package com.vigil.alarm;

import java.util.Map;

import org.tomlj.TomlTable;
import com.vigil.exception.InvalidConfigurationException;

public class AlarmConfig {

    private final String monitorName;

    private final double highWarning;
    private final double highWarningClear;

    private final double highAlarm;
    private final double highAlarmClear;

    private final double lowWarning;
    private final double lowWarningClear;

    private final double lowAlarm;
    private final double lowAlarmClear;

    private final long activationDelayMs;
    private final long clearDelayMs;

    private AlarmConfig(String name,
                       double highWarn,
                       double highWarnClear,
                       double highAlarm,
                       double highAlarmClear,
                       double lowWarning,
                       double lowWarningClear,
                       double lowAlarm,
                       double lowAlarmClear,
                       long activationDelayMs,
                       long clearDelayMs) {

        this.monitorName = name;
    
        this.highWarning = highWarn;
        this.highWarningClear = highWarnClear;

        this.highAlarm = highAlarm;
        this.highAlarmClear = highAlarmClear;

        this.lowWarning = lowWarning;
        this.lowWarningClear = lowWarningClear;

        this.lowAlarm = lowAlarm;
        this.lowAlarmClear = lowAlarmClear;

        this.activationDelayMs = activationDelayMs;
        this.clearDelayMs = clearDelayMs;
    }

    private static double requireDouble(TomlTable table, String monitorId, String key) {
        Double value = table.getDouble(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required double field 'highWarning' in alarm configuration for monitor '" + monitorId + "'");
        }
        return value;
    }

    private static long requireLong(TomlTable table, String monitorId, String key) {
        Long value = table.getLong(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required long field 'highWarning' in alarm configuration for monitor '" + monitorId + "'");
        }
        return value;
    }

    private static double mapRequireDouble(Map<String, Object> table, String monitorId, String key) {
        Double value = (Double)table.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required double field 'highWarning' in alarm configuration for monitor '" + monitorId + "'");
        }
        return value;
    }

    private static long mapRequireLong(Map<String, Object> table, String monitorId, String key) {
        
        Long value = (Long)table.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required long field 'highWarning' in alarm configuration for monitor '" + monitorId + "'");
        }
        return value;
    }

    private void validate() {
        if (this.lowAlarm > this.lowAlarmClear){
            throw new InvalidConfigurationException("Low Alarm Clear setpoint must be greater than or equal to Low Alarm setpoint (" + this.monitorName + ")!");
        }
        if (this.lowWarning > this.lowWarningClear){
            throw new InvalidConfigurationException("Low Warning Clear setpoint must be greater than or equal to Low Warning setpoint (" + this.monitorName + ")!");
        }
        if (this.highAlarm < this.highAlarmClear){
            throw new InvalidConfigurationException("High Alarm Clear setpoint must be less than or equal to High Alarm setpoint (" + this.monitorName + ")!");
        }
        if (this.highWarning < this.highWarningClear){
            throw new InvalidConfigurationException("High Warning Clear setpoint must be less than or equal to High Warning setpoint (" + this.monitorName + ")!");
        }
        if (this.lowAlarm > this.lowWarning) {
            throw new InvalidConfigurationException("Low Alarm setpoint must be less than Low Warning setpoing (" + this.monitorName + ")!");
        }
        if (this.highAlarm < this.highWarning) {
            throw new InvalidConfigurationException("High Alarm setpoint must be greater than High Warning setpoing (" + this.monitorName + ")!");
        }
        if (this.lowWarning >= this.highWarning) {
            throw new InvalidConfigurationException( "Low Warning must be less than High Warning (" + monitorName + ")!");
        }
        if (this.lowAlarm >= this.highAlarm) {
            throw new InvalidConfigurationException( "Low Alarm must be less than High Alarm (" + monitorName + ")!");
        }
        if (this.activationDelayMs < 0) {
            throw new InvalidConfigurationException( "Activation Delay (ms) must be greater than 0 (" + monitorName + ")!");
        }

        if (this.clearDelayMs < 0) {
            throw new InvalidConfigurationException( "Clear Delay (ms) must be greater than 0 (" + monitorName + ")!");
        }
    }

    public static AlarmConfig fromToml(String id, TomlTable table) {
        AlarmConfig config =  new AlarmConfig(id,
                       requireDouble(table, id, "highWarning"),
                       requireDouble(table, id, "highWarningClear"),
                       requireDouble(table, id, "highAlarm"),
                       requireDouble(table, id, "highAlarmClear"),
                       requireDouble(table, id, "lowWarning"),
                       requireDouble(table, id, "lowWarningClear"),
                       requireDouble(table, id, "lowAlarm"),
                       requireDouble(table, id, "lowAlarmClear"),
                       requireLong(table, id, "activationDelayMs"),
                       requireLong(table, id, "clearDelayMs"));

        config.validate();

        return config;
    }

    public static AlarmConfig fromMap(String id, Map<String, Object> table) {
        AlarmConfig config =  new AlarmConfig(id,
                       mapRequireDouble(table, id, "highWarning"),
                       mapRequireDouble(table, id, "highWarningClear"),
                       mapRequireDouble(table, id, "highAlarm"),
                       mapRequireDouble(table, id, "highAlarmClear"),
                       mapRequireDouble(table, id, "lowWarning"),
                       mapRequireDouble(table, id, "lowWarningClear"),
                       mapRequireDouble(table, id, "lowAlarm"),
                       mapRequireDouble(table, id, "lowAlarmClear"),
                       mapRequireLong(table, id, "activationDelayMs"),
                       mapRequireLong(table, id, "clearDelayMs"));

        config.validate();

        return config;
    }

    public String getMonitorName() {
        return this.monitorName;
    }
    
    public double getHighWarning(){
        return this.highWarning;
    }
    
    public double getHighWarningClear(){
        return this.highWarningClear;
    }

    public double getHighAlarm(){
        return this.highAlarm;
    }
    
    public double getHighAlarmClear(){
        return this.highAlarmClear;
    }

    public double getLowWarning() {
        return this.lowWarning;
    }

    public double getLowWarningClear() {
        return this.lowWarningClear;
    }

    public double getLowAlarm() {
        return this.lowAlarm;
    }

    public double getLowAlarmClear() {
        return this.lowAlarmClear;
    }

    public long getActivationDelayMs() {
        return this.activationDelayMs;
    }
    
    public long getClearDelayMs() {
        return this.clearDelayMs;
    }
}
