package com.vigil.alarm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AlarmAcknowledgeQueue implements AlarmEventSink {

    private final BlockingQueue<AlarmAcknowledgeOut> queue =
        new LinkedBlockingQueue<>();

    @Override
    public void submit(AlarmAcknowledgeOut acknowledgement) {
        queue.offer(acknowledgement);
    }

    public AlarmAcknowledgeOut poll() {
        return queue.poll();
    }
}