package com.vigil.dispatcher;

import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vigil.alarm.AlarmMessage;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.AlarmAcknowledgeOut;
import com.vigil.message.VigilMessage;
import com.vigil.monitor.TelemetryOut;
import com.vigil.app.AppConfig;

public abstract class Dispatcher {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    protected final AppConfig appConfig;
    public abstract void start();
    public abstract void stop();
    public abstract void sendAlarm(AlarmMessage<?> result);
    public abstract void sendValue(TelemetryOut<?> value);
    public abstract void sendAlarmAcknowledgement(AlarmAcknowledgeOut acknowledgement);
    public abstract void sendAlarmAcknowledgeFail(AlarmAcknowledgeFail failure);

    private final ObjectMapper objectMapper;
    
    protected Dispatcher(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    protected String serialize(VigilMessage message) {
        try {
            ObjectNode payload = objectMapper.valueToTree(message);
            payload.put("type", message.type().name());
            payload.put("deviceId", appConfig.getDeviceId());
            payload.put("deviceName", appConfig.getDeviceName());
            
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "Failed to serialize Vigil message", e
            );
        }
    }
}
