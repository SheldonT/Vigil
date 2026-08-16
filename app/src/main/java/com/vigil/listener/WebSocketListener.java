package com.vigil.listener;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;

import com.vigil.alarm.AlarmMessage;
import com.vigil.config.ConfigValidator;
import com.vigil.message.AlarmAcknowledgeIn;
import com.vigil.message.VigilMessage;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocketListener extends Listener implements AlarmAcknowledger{

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

    public WebSocketListener(
        Function<UUID, VigilMessage> callback,
        Configuration config) {

        this.ackCallback = callback;

        this.client = new WebSocketClient(
            URI.create(config.host())
        ) {
            @Override
            public void onOpen(ServerHandshake handshake) {
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

                logger.info("WebSocket connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                logger.severe(
                    "WebSocket error: " + ex.getMessage()
                );
            }
        };
    }
    
    @Override
    public void start(){
        this.client.connect();
    }

    @Override
    public void stop(){
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

}
