package com.vigil.dispatcher;

import java.util.Map;


import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import com.vigil.alarm.AlarmResult;
import com.vigil.config.ConfigValidator;
import com.vigil.monitor.MonitorReading;

public class MqttDispatcher extends Dispatcher{

        public record Configuration(String host, String topic, int port) implements DispatcherConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, "MQTT Dispatcher Map");
            Configuration config = new Configuration(

                ConfigValidator.requireString(validMap, "MQTT Dispatcher", "host"),
                ConfigValidator.requireString(validMap, "MQTT Dispatcher", "topic"),
                ConfigValidator.requirePort(validMap, "MQTT Dispatcher", "port")
            );

            return config;
        }

        @Override
        public String getType(){
            return "Mqtt";
        }
    }

    private final Configuration config;
    private Mqtt5AsyncClient client;

    public MqttDispatcher(Configuration config) {

        this.config = config;

        this.client = this.createClient(config);
        this.connect();
    }

    private Mqtt5AsyncClient createClient(Configuration config){

        return MqttClient.builder()
        .useMqttVersion5()
        .serverHost(config.host())
        .serverPort(config.port())
        .automaticReconnectWithDefaultConfig()
        .buildAsync();
    }

    private void connect() {
        this.client.connect().whenComplete(
            (connAck, throwable) -> {
                if (throwable != null) {
                    logger.warning("Mqtt connection failed: " + throwable.getMessage());
                }
                else {
                    logger.info("Mqtt Connected");
                }
            }
        );
    }

    @Override
    public void sendAlarm(AlarmResult result){

        String payload = result.timestampNow + "," + result.name + "," + result.status + "," + result.value;
        String topic = this.config.topic() + "/alarm";

        this.client.publishWith()
        .topic(topic)
        .payload(payload.getBytes())
        .send();
    }

    @Override
    public void sendValue(MonitorReading<?> result){
        String payload = result.timestamp() + "," + result.name() + "," + result.value();
        String topic = this.config.topic() + "/telemetry";
        
        this.client.publishWith()
        .topic(topic)
        .payload(payload.getBytes())
        .send();
    }
}
