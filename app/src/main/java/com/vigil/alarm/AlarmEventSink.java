package com.vigil.alarm;

public interface AlarmEventSink {
    void submit(AlarmAcknowledgeOut acknowledgement);
}
