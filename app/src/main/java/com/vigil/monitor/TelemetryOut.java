package com.vigil.monitor;

import java.time.Instant;

import com.vigil.app.VigilMessage;
import com.vigil.app.MessageType;

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