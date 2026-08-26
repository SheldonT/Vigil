package com.vigil.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vigil.message.MessageType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlarmAcknowledgeIn(
    @JsonProperty("alarmId") UUID alarmId,
    @JsonProperty("deviceId") String deviceId,
    @JsonProperty("deviceName") String deviceName
) implements VigilCommand {

        @Override
        public MessageType type(){
                return MessageType.COMMAND;
        }

        @Override
        public CommandType commandType(){
                return CommandType.ACKNOWLEDGE;
        }
}