package com.vigil.monitor;

import java.time.Instant;

import com.vigil.message.MessageType;
import com.vigil.message.VigilMessage;

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