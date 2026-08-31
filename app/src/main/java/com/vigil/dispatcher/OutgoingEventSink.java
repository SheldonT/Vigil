package com.vigil.dispatcher;

import com.vigil.message.VigilMessage;

public interface OutgoingEventSink {
    void submit(VigilMessage acknowledgement);
}
