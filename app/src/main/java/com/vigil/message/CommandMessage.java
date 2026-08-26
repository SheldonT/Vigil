package com.vigil.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.vigil.command.CommandType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CommandMessage(
    @JsonProperty("commandType") CommandType commandType,
    @JsonProperty("payload") JsonNode payload
) implements VigilMessage {

    @Override
    public MessageType type() {
        return MessageType.COMMAND;
    }
}