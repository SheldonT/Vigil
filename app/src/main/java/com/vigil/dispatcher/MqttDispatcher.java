package com.vigil.dispatcher;

import java.util.Map;


import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.vigil.alarm.AlarmMessage;
import com.vigil.config.ConfigValidator;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.AlarmAcknowledgeOut;
import com.vigil.monitor.TelemetryOut;

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

        super();

        this.config = config;

        this.client = this.createClient(config);
        //this.connect();
    }

    @Override
    public void start(){
        this.connect();
    }

    @Override
    public void stop(){
        this.disconnect();
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
    public void sendAlarm(AlarmMessage<?> result){

        String payload = this.serialize(result);

        //String payload = result.lastUpdated()+ "," + result.alarmId() + "," + result.name() + "," + result.status() + "," + result.value();
        //String topic = this.config.topic() + "/alarm";

        this.client.publishWith()
        .topic(this.config.topic)
        .payload(payload.getBytes())
        .send();
    }

    @Override
    public void sendValue(TelemetryOut<?> result){
        //String payload = result.timestamp() + "," + result.name() + "," + result.value();
        String payload = this.serialize(result);
        //String topic = this.config.topic() + "/telemetry";
        
        this.client.publishWith()
        .topic(this.config.topic())
        .payload(payload.getBytes())
        .send();
    }

    @Override
    public void sendAlarmAcknowledgement(AlarmAcknowledgeOut acknowledgement){
        
        String payload = this.serialize(acknowledgement);
        //String payload = acknowledgement.acknowledgedAt()+ "," + acknowledgement.alarmId() + "," + acknowledgement.source() + "," + "ACK";
        //String topic = this.config.topic() + "/acknowledge";

        this.client.publishWith()
        .topic(this.config.topic())
        .payload(payload.getBytes())
        .send();
    }

    @Override
    public void sendAlarmAcknowledgeFail(AlarmAcknowledgeFail failure) {
        String payload = this.serialize(failure);

        this.client.publishWith()
        .topic(this.config.topic())
        .payload(payload.getBytes())
        .send();
    }
}
