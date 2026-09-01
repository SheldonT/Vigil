package com.vigil.command;

import java.util.UUID;

public record AlarmAcknowledgePayload(
    UUID alarmId,
    String deviceId,
    String deviceName) {

    public AlarmAcknowledgePayload {
        if (alarmId == null) {
            throw new IllegalArgumentException(
                "alarmId is required"
            );
        }
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException(
                "deviceId is required"
            );
        }
        if (deviceName == null || deviceName.isBlank()) {
            throw new IllegalArgumentException(
                "deviceName is required"
            );
        }
    }
}