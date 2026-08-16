package com.vigil.alarm;

import com.vigil.message.AlarmAcknowledgeOut;

public interface AlarmEventSink {
    void submit(AlarmAcknowledgeOut acknowledgement);
}
