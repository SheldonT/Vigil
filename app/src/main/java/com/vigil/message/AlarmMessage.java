package com.vigil.message;

import java.time.Instant;
import java.util.UUID;

import com.vigil.alarm.Status;

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