package com.vigil.alarm;

import java.time.Instant;
import java.util.UUID;

import com.vigil.app.MessageType;
import com.vigil.app.VigilMessage;

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