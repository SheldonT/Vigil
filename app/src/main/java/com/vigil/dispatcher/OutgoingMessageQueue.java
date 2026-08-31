package com.vigil.dispatcher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.vigil.message.VigilMessage;

public class OutgoingMessageQueue implements OutgoingEventSink {

    private final BlockingQueue<VigilMessage> queue =
        new LinkedBlockingQueue<>();

    @Override
    public void submit(VigilMessage acknowledgement) {
        queue.offer(acknowledgement);
    }

    public VigilMessage poll() {
        return queue.poll();
    }

    public VigilMessage take() throws InterruptedException {
        return queue.take();
    }
}