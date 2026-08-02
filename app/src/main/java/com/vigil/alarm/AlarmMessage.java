package com.vigil.alarm;

import java.time.Instant;
import java.util.UUID;

public record AlarmMessage<T>(
        UUID alarmId,
        String name,
        T value,
        Status status,
        boolean acknowledged,
        Instant activatedAt,
        Instant acknowledgedAt,
        Instant lastUpdated
) {}