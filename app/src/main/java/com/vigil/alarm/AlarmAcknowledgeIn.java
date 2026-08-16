package com.vigil.alarm;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vigil.app.MessageType;
import com.vigil.app.VigilMessage;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlarmAcknowledgeIn(
    @JsonProperty("alarmId") UUID alarmId
) implements VigilMessage {

        @Override
        public MessageType type(){
                return MessageType.ACKNOWLEDGE_ALARM;
        }
}