package com.vigil.app;

import java.util.Map;

import com.vigil.exception.InvalidConfigurationException;
import com.vigil.config.ConfigValidator;

public class AppConfig {
    
    private final String configId;
    private final String deviceName;
    private final String deviceId;
    private final long pollingIntervalMs;

    private AppConfig(String configId, String deviceName, String deviceId, long pollingIntervalMs){

        this.configId = configId;
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.pollingIntervalMs = pollingIntervalMs;
    }

    private void validate() {

        if (this.pollingIntervalMs < 0) {
            throw new InvalidConfigurationException( "loop must be greater than 0!");
        }
    }

    public static AppConfig fromMap(String configId, Map<String, Object> table) {
        AppConfig config =  new AppConfig(configId,
            ConfigValidator.requireString(table, configId, "name"),
            ConfigValidator.requireString(table, configId, "id"),
            ConfigValidator.requireLong(table, configId, "pollingIntervalMs")
        );

        config.validate();

        return config;
    }

    public String getConfigId() {
        return this.configId;
    }
    public String getDeviceId() {
        return this.deviceId;
    }

    public String getDeviceName() {
        return this.deviceName;
    }
    
    public long getPollingIntervalMs() {
        return this.pollingIntervalMs;
    }
}
