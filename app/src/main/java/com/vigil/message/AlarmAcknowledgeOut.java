package com.vigil.message;

import java.time.Instant;
import java.util.UUID;

public record AlarmAcknowledgeOut(
    UUID alarmId,
    Instant acknowledgedAt,
    String source
)implements VigilMessage {

        @Override
        public MessageType type(){
                return MessageType.ALARM_ACKNOWLEDGED;
        }
}