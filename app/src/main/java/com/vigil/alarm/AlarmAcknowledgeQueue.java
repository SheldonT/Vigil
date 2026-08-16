package com.vigil.alarm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.vigil.message.AlarmAcknowledgeOut;

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

    public AlarmAcknowledgeOut take() throws InterruptedException {
        return queue.take();
    }
}