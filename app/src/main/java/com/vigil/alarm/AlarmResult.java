package com.vigil.alarm;

import java.time.Instant;

public record AlarmResult<T> (
    String name,
    T value,
    Status status,
    Instant timestampNow
) {}
