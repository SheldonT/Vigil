package com.vigil.listener;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vigil.message.AlarmAcknowledgeIn;
import com.vigil.message.MessageType;
import com.vigil.message.VigilMessage;

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

    protected abstract void handleMessage(VigilMessage msg);

    protected Optional<VigilMessage> deserialize(String json) {

        try {
            JsonNode node = this.objectMapper.readTree(json);

            JsonNode typeNode = node.get("type");

            if (typeNode == null) {
                throw new IllegalArgumentException(
                    "Vigil message is missing type"
                );
            }

            MessageType type;

            try{
                type = MessageType.valueOf(typeNode.asText());
            } catch(IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Unsupported incoming message type: " + typeNode.asText()
                );
            }

            return switch (type) {

                case ACKNOWLEDGE_ALARM ->
                    Optional.of(
                        objectMapper.treeToValue(
                            node,
                            AlarmAcknowledgeIn.class
                        )
                    );
  
                default ->Optional.empty();
            };

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                "Invalid Vigil message JSON", e
            );
        }
    }

}