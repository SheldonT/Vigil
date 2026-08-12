package com.vigil.alarm;

import java.time.Instant;
import java.util.UUID;

public record AlarmAcknowledge(
    UUID alarmId,
    Instant acknowledgedAt,
    String source
){}