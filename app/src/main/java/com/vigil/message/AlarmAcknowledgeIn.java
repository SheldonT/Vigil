package com.vigil.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlarmAcknowledgeIn(
    @JsonProperty("alarmId") UUID alarmId,
    @JsonProperty("deviceId") String deviceId,
    @JsonProperty("deviceName") String deviceName
) implements VigilMessage {

        @Override
        public MessageType type(){
                return MessageType.ACKNOWLEDGE_ALARM;
        }
}