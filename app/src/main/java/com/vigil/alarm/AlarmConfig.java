package com.vigil.alarm;

import java.util.Map;

// import com.vigil.exception.InvalidConfigurationException;
// import com.vigil.config.ConfigValidator;

public abstract class AlarmConfig<T> {

    private final String monitorName;

    private final long activationDelayMs;
    private final long clearDelayMs;

    private AlarmConfig(String name,
                       long activationDelayMs,
                       long clearDelayMs) {

        this.monitorName = name;

        this.activationDelayMs = activationDelayMs;
        this.clearDelayMs = clearDelayMs;
    }

    public abstract void validate();

    public abstract T fromMap(String id, Map<String, Object> table);

    public String getMonitorName() {
        return this.monitorName;
    }

    public long getActivationDelayMs() {
        return this.activationDelayMs;
    }
    
    public long getClearDelayMs() {
        return this.clearDelayMs;
    }
}
