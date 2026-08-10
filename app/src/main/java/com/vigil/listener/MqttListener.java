package com.vigil.listener;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import com.vigil.alarm.AlarmMessage;
import com.vigil.config.ConfigValidator;

public class MqttListener implements Listener, AlarmAcknowledger {

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
        this.ackCallback = callback;
        this.client = createClient(config);
        this.topic = config.topic() + "/ack";
    }

    private Mqtt5AsyncClient createClient(Configuration config){

        return MqttClient.builder()
        .useMqttVersion5()
        .serverHost(config.host())
        .serverPort(config.port())
        .automaticReconnectWithDefaultConfig()
        .buildAsync();
    }

    private void handleMessage(Mqtt5Publish publish) {
        try{
            String message = StandardCharsets.UTF_8
                .decode(publish.getPayload().orElseThrow())
                .toString();
            UUID alarmId = UUID.fromString(message);

            AlarmMessage<?> ackMessage = this.acknowledgeAlarm(alarmId);

            logger.info("Evaluating alarm " + alarmId + " : " + ackMessage);
            if (ackMessage == null){
                logger.info("Alarm with id " + alarmId + " doesn't exist or has cleared.");
            }
            else {
                logger.info("Alarm " + alarmId + " acknowledged at " + ackMessage.acknowledgedAt());
            }
            
        } catch (Exception e) {
            logger.warning("Invalid UUID: " + e);
        }
    }

    @Override
    public void start(){
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
                                this.handleMessage(publish);
                            })
                            .send();
                });
    }

    @Override
    public void stop(){}

    @Override
    public AlarmMessage<?> acknowledgeAlarm (UUID alarmId){
        return ackCallback.apply(alarmId);
    }
}
