package com.vigil.alarm;

import java.time.Instant;
import java.util.UUID;

import com.vigil.message.MessageType;
import com.vigil.message.VigilMessage;

public record AlarmMessage<T> (
        UUID alarmId,
        String name,
        T value,
        Status status,
        boolean acknowledged,
        Instant activatedAt,
        Instant acknowledgedAt,
        Instant lastUpdated
) implements VigilMessage {

        @Override
        public MessageType type(){
                return MessageType.ALARM;
        }
}