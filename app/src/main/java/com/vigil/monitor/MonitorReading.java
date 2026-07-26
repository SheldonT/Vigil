package com.vigil.monitor;

import java.time.Instant;

public record MonitorReading<T> (
    String name,
    T value,
    Instant timestamp
) {}
