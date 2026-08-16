package com.vigil.message;

import java.util.UUID;

public record AlarmAcknowledgeFail(
    UUID alarmId,
    String reason
) implements VigilMessage {
        @Override
        public MessageType type(){
            return MessageType.ALARM_ACKNOWLEDGE_FAILED;
        }
}

