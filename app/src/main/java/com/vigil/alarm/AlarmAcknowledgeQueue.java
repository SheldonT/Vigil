package com.vigil.alarm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AlarmAcknowledgeQueue implements AlarmEventSink {

    private final BlockingQueue<AlarmAcknowledge> queue =
        new LinkedBlockingQueue<>();

    @Override
    public void submit(AlarmAcknowledge acknowledgement) {
        queue.offer(acknowledgement);
    }

    public AlarmAcknowledge poll() {
        return queue.poll();
    }
}