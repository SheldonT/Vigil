package com.vigil.listener;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vigil.command.CommandType;
import com.vigil.message.CommandMessage;
import com.vigil.message.MessageType;

import com.vigil.command.VigilCommand;

public abstract class Listener {

    public abstract void start();
    public abstract void stop();

    private final ObjectMapper objectMapper;
    
    protected Listener() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    // protected abstract void handleMessage(VigilMessage msg);

    protected Optional<VigilCommand> deserialize(String json) {

        try {
            JsonNode node = objectMapper.readTree(json);

            MessageType type = getMessageType(node);

            if (type != MessageType.COMMAND) {
               return Optional.empty();
            }
            
            return deserializeCommand(node);

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                "Invalid Vigil message JSON", e
            );
        }
    }

    private MessageType getMessageType(JsonNode node) {

        JsonNode typeNode = node.get("type");

        if (typeNode == null) {
            throw new IllegalArgumentException(
                "Vigil message is missing type"
            );
        }

        try {
            return MessageType.valueOf(typeNode.asText());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unsupported incoming message type: " + typeNode.asText()
            );
        }
    }

    private CommandType getCommandType(JsonNode node) {

        JsonNode commandTypeNode = node.get("commandType");

        if (commandTypeNode == null) {
            throw new IllegalArgumentException(
                "Vigil command type is missing"
            );
        }

        try {
            return CommandType.valueOf(commandTypeNode.asText());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unsupported incoming command type: "
                + commandTypeNode.asText()
            );
        }
    }

    private Optional<VigilCommand> deserializeCommand(
        JsonNode node
    ) throws JsonProcessingException {

        return Optional.of(
            objectMapper.treeToValue(
                node,
                CommandMessage.class
            )
        );
    }

}