package com.vigil.app;

import java.util.Map;

import com.vigil.exception.InvalidConfigurationException;
import com.vigil.config.ConfigValidator;

public class AppConfig {
    
    private final String id;
    private final long pollingIntervalMs;

    private AppConfig(String id, long pollingIntervalMs){

        this.id = id;
        this.pollingIntervalMs = pollingIntervalMs;
    }

    private void validate() {

        if (this.pollingIntervalMs < 0) {
            throw new InvalidConfigurationException( "loop must be greater than 0!");
        }
    }

    public static AppConfig fromMap(String id, Map<String, Object> table) {
        AppConfig config =  new AppConfig(id,
            ConfigValidator.requireLong(table, id, "pollingIntervalMs"));

        config.validate();

        return config;
    }

    public String getConfigId() {
        return this.id;
    }
    
    public long getPollingIntervalMs() {
        return this.pollingIntervalMs;
    }
}
