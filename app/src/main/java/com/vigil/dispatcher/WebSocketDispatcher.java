package com.vigil.dispatcher;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.vigil.app.AppConfig;
import com.vigil.config.ConfigValidator;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.AlarmAcknowledgeOut;
import com.vigil.message.AlarmMessage;
import com.vigil.message.TelemetryOut;
import com.vigil.message.VigilMessage;

public class WebSocketDispatcher extends Dispatcher{
    private static final long RECONNECT_DELAY_MS = 2000;
    private static final long OFFLINE_LOG_THROTTLE_MS = 30000;

    public record Configuration(String host) implements DispatcherConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, "WebSocket Dispatcher Map");
            Configuration config = new Configuration(

                ConfigValidator.requireString(validMap, "WebSocket Dispatcher", "host")
            );

            return config;
        }
        

        @Override
        public String getType(){
            return "WebSocket";
        }
    }

    private final WebSocketClient client;
    private final ScheduledExecutorService reconnectExecutor;
    private final AtomicBoolean running;
    private final AtomicBoolean reconnectScheduled;
    private long nextDisconnectedLogAtMs;
    private int suppressedDisconnectedLogs;
    private long nextReconnectLogAtMs;
    private int suppressedReconnectLogs;

    public WebSocketDispatcher (Configuration config, AppConfig appConfig){

        super(appConfig);

        this.reconnectExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "vigil-ws-dispatcher-reconnect");
            thread.setDaemon(true);
            return thread;
        });
        this.running = new AtomicBoolean(false);
        this.reconnectScheduled = new AtomicBoolean(false);
        this.nextDisconnectedLogAtMs = 0;
        this.suppressedDisconnectedLogs = 0;
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
                // Dispatcher doesn't need incoming messages
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

    // @Override
    // public void sendAlarm(AlarmMessage<?> result) {
    //     String payload = this.serialize(result);

    //     this.sendIfConnected(payload, "alarm");
    // };

    // @Override
    // public void sendValue(TelemetryOut<?> value){
    //     String payload = this.serialize(value);

    //     this.sendIfConnected(payload, "telemetry");
    // }

    @Override
    public void send(VigilMessage message){
        String payload = this.serialize(message);

        this.sendIfConnected(payload, "telemetry");
    }

    @Override
    public void sendAlarmAcknowledgement(AlarmAcknowledgeOut acknowledgement){
        String payload = this.serialize(acknowledgement);

        this.sendIfConnected(payload, "alarm acknowledgement");
    }

    @Override
    public void sendAlarmAcknowledgeFail(AlarmAcknowledgeFail failure) {
        String payload = this.serialize(failure);
        this.sendIfConnected(payload, "alarm acknowledgement failure");
    }

    private void sendIfConnected(String payload, String payloadType) {
        if (!this.client.isOpen()) {
            logDisconnectedSendThrottled(payloadType);
            return;
        }

        this.client.send(payload);
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

    private synchronized void logDisconnectedSendThrottled(String payloadType) {
        long now = System.currentTimeMillis();

        if (now >= this.nextDisconnectedLogAtMs) {
            String message = "Skipping " + payloadType + " send because WebSocket is not connected";
            if (this.suppressedDisconnectedLogs > 0) {
                message += " (suppressed " + this.suppressedDisconnectedLogs + " similar messages)";
            }

            logger.warning(message);
            this.nextDisconnectedLogAtMs = now + OFFLINE_LOG_THROTTLE_MS;
            this.suppressedDisconnectedLogs = 0;
            return;
        }

        this.suppressedDisconnectedLogs++;
    }

    private synchronized void logReconnectThrottled(String message) {
        long now = System.currentTimeMillis();

        if (now >= this.nextReconnectLogAtMs) {
            if (this.suppressedReconnectLogs > 0) {
                logger.info(
                    "WebSocket reconnect loop active (suppressed "
                        + this.suppressedReconnectLogs
                        + " similar messages)"
                );
            }

            logger.info(message);
            this.nextReconnectLogAtMs = now + OFFLINE_LOG_THROTTLE_MS;
            this.suppressedReconnectLogs = 0;
            return;
        }

        this.suppressedReconnectLogs++;
    }
}
