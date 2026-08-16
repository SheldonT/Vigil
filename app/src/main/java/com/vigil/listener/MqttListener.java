package com.vigil.listener;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.vigil.alarm.AlarmAcknowledgeIn;
import com.vigil.alarm.AlarmMessage;
import com.vigil.app.VigilMessage;
import com.vigil.config.ConfigValidator;

public class MqttListener extends Listener implements AlarmAcknowledger {

    private static final Logger logger = Logger.getLogger(MqttListener.class.getName());

    public record Configuration(String host, String topic, int port) implements ListenerConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, "MQTT Dispatcher Map");
            Configuration config = new Configuration(

                ConfigValidator.requireString(validMap, "MQTT Listener", "host"),
                ConfigValidator.requireString(validMap, "MQTT Listener", "topic"),
                ConfigValidator.requirePort(validMap, "MQTT Listener", "port")
            );

            return config;
        }

        @Override
        public String getType(){
            return "Mqtt";
        }
    }

    private final Function<UUID, AlarmMessage<?>> ackCallback;
    private final Mqtt5AsyncClient client;
    private final String topic;

    public MqttListener(Function<UUID, AlarmMessage<?>> callback, Configuration config){
        super();
        this.ackCallback = callback;
        this.client = createClient(config);
        this.topic = config.topic();
    }

    private Mqtt5AsyncClient createClient(Configuration config){

        return MqttClient.builder()
        .useMqttVersion5()
        .serverHost(config.host())
        .serverPort(config.port())
        .automaticReconnectWithDefaultConfig()
        .buildAsync();
    }

    private void publishMessage(Mqtt5Publish publish) {
        try{
            String message = StandardCharsets.UTF_8
                .decode(publish.getPayload().orElseThrow())
                .toString();

            this.deserialize(message).ifPresent(this::handleMessage);
            
        } catch (Exception e) {
            logger.warning("Unsupported message type received by MQTT Listener: " + e);
        }
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
                    "Unsupported message type: " + msg.type()
                );
        }
    }

    @Override
    public void start(){
        this.connect();
    }

    @Override
    public void stop(){
        this.disconnect();
    }

    private void connect(){
        client.connectWith()
            .send()
            .whenComplete((connAck, throwable) -> {

                if (throwable != null) {
                    logger.severe("MQTT connection failed: " + throwable.getMessage());
                    return;
                }

                logger.info("Connected to MQTT broker and subscribing to topic: " + this.topic);
                client.subscribeWith()
                        .topicFilter(this.topic)
                        .callback(publish -> {
                            this.publishMessage(publish);
                        })
                        .send();
            });
    }

    private void disconnect() {
        this.client.disconnect()
        .whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.warning(
                    "MQTT disconnect failed: " + throwable.getMessage()
                );
            } else {
                logger.info("Disconnected from MQTT broker");
            }
        });
    }

    @Override
    public AlarmMessage<?> acknowledgeAlarm (UUID alarmId){
        return ackCallback.apply(alarmId);
    }
}
