package com.vigil.dispatcher;

import java.net.URI;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.vigil.alarm.AlarmMessage;
import com.vigil.config.ConfigValidator;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.AlarmAcknowledgeOut;
import com.vigil.monitor.TelemetryOut;

public class WebSocketDispatcher extends Dispatcher{
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

    public WebSocketDispatcher (Configuration config){

        this.client = new WebSocketClient(
            URI.create(config.host())
        ) {
            @Override
            public void onOpen(ServerHandshake handshake) {
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
    public void sendAlarm(AlarmMessage<?> result) {
        String payload = this.serialize(result);

        this.client.send(payload);
    };

    @Override
    public void sendValue(TelemetryOut<?> value){
        String payload = this.serialize(value);

        this.client.send(payload);
    }

    @Override
    public void sendAlarmAcknowledgement(AlarmAcknowledgeOut acknowledgement){
        String payload = this.serialize(acknowledgement);
        System.out.println(payload);
        this.client.send(payload);
    }

    @Override
    public void sendAlarmAcknowledgeFail(AlarmAcknowledgeFail failure) {
        String payload = this.serialize(failure);
        this.client.send(payload);
    }
}
