package com.vigil.message;

import java.time.Instant;

public record TelemetryOut<T> (
    String name,
    T value,
    Instant timestamp
) implements VigilMessage {

        @Override
        public MessageType type(){
                return MessageType.TELEMETRY;
        }
}