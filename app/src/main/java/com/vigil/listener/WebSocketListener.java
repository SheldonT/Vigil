package com.vigil.listener;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Logger;

import com.vigil.alarm.AlarmMessage;
import com.vigil.config.ConfigValidator;
import com.vigil.message.AlarmAcknowledgeIn;
import com.vigil.message.VigilMessage;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketListener extends Listener implements AlarmAcknowledger{
    private static final long RECONNECT_DELAY_MS = 2000;
    private static final long RECONNECT_LOG_THROTTLE_MS = 30000;

     public record Configuration(String host) implements ListenerConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, " Web Socket Listener Map");
            Configuration config = new Configuration(

                ConfigValidator.requireString(validMap, "WebSocket Listener", "host")
            );

            return config;
        }

        @Override
        public String getType(){
            return "WebSocket";
        }
    }

    private static final Logger logger =
        Logger.getLogger(WebSocketListener.class.getName());

    private final Function<UUID, VigilMessage> ackCallback;
    private final WebSocketClient client;
    private final ScheduledExecutorService reconnectExecutor;
    private final AtomicBoolean running;
    private final AtomicBoolean reconnectScheduled;
    private long nextReconnectLogAtMs;
    private int suppressedReconnectLogs;

    public WebSocketListener(
        Function<UUID, VigilMessage> callback,
        Configuration config) {

        this.ackCallback = callback;
        this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "vigil-ws-listener-reconnect");
            thread.setDaemon(true);
            return thread;
        });
        this.running = new AtomicBoolean(false);
        this.reconnectScheduled = new AtomicBoolean(false);
        this.nextReconnectLogAtMs = 0;
        this.suppressedReconnectLogs = 0;

        this.client = new WebSocketClient(
            URI.create(config.host())
        ) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                reconnectScheduled.set(false);
                logger.info("Connected to WebSocket server");
            }

            @Override
            public void onMessage(String message) {
                try {
                    deserialize(message).ifPresent(WebSocketListener.this::handleMessage);
                } catch (Exception e) {
                    logger.warning("WebSocket listener error: " + e.getMessage());
                }
            }

            @Override
            public void onClose(
                    int code,
                    String reason,
                    boolean remote) {

                logReconnectThrottled("WebSocket connection closed: " + reason);
                scheduleReconnect("connection closed");
            }

            @Override
            public void onError(Exception ex) {
                logReconnectThrottled("WebSocket error: " + ex.getMessage());
                scheduleReconnect("connection error");
            }
        };
    }
    
    @Override
    public void start(){
        this.running.set(true);
        this.client.connect();
    }

    @Override
    public void stop(){
        this.running.set(false);
        this.reconnectExecutor.shutdownNow();
        this.client.close();
    }

    @Override
    protected void handleMessage(VigilMessage msg) {
        switch (msg.type()) {

            case ACKNOWLEDGE_ALARM -> {
                AlarmAcknowledgeIn acknowledgement =
                    (AlarmAcknowledgeIn) msg;

                acknowledgeAlarm(acknowledgement.alarmId());
            }

            default ->
                logger.warning(
                    "Unsupported message type received by WebSocket Listener: " + msg.type()
                );
        }
            
    }

    @Override
    public VigilMessage acknowledgeAlarm(UUID alarmId){
        return this.ackCallback.apply(alarmId);
    }

    private void scheduleReconnect(String reason) {
        if (!this.running.get()) {
            return;
        }

        if (!this.reconnectScheduled.compareAndSet(false, true)) {
            return;
        }

        logReconnectThrottled(
            "Scheduling WebSocket reconnect in " + RECONNECT_DELAY_MS + "ms after " + reason
        );

        this.reconnectExecutor.schedule(() -> {
            this.reconnectScheduled.set(false);

            if (!this.running.get() || this.client.isOpen()) {
                return;
            }

            try {
                logReconnectThrottled("Attempting WebSocket reconnect...");
                this.client.reconnect();
            } catch (Exception ex) {
                logReconnectThrottled("WebSocket reconnect attempt failed: " + ex.getMessage());
                scheduleReconnect("failed reconnect attempt");
            }
        }, RECONNECT_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private synchronized void logReconnectThrottled(String message) {
        long now = System.currentTimeMillis();

        if (now >= this.nextReconnectLogAtMs) {
            if (this.suppressedReconnectLogs > 0) {
                logger.info(
                    "WebSocket listener reconnect loop active (suppressed "
                        + this.suppressedReconnectLogs
                        + " similar messages)"
                );
            }

            logger.info(message);
            this.nextReconnectLogAtMs = now + RECONNECT_LOG_THROTTLE_MS;
            this.suppressedReconnectLogs = 0;
            return;
        }

        this.suppressedReconnectLogs++;
    }

}
