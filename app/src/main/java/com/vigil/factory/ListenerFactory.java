package com.vigil.factory;

import java.util.Map;

import com.vigil.alarm.AlarmEngine;
import com.vigil.listener.Listener;
import com.vigil.listener.MqttListener;
import com.vigil.listener.WebSocketListener;

public class ListenerFactory{

    public static Listener create(Map<String, Object> config, AlarmEngine alarmEngine){

        String listenerType = (String)config.get("type");

        switch(listenerType){
            
            case "MQTT":
                return new MqttListener(alarmEngine::acknowledgeAlarm, MqttListener.Configuration.fromMap(config));
            case "WebSocket":
                return new WebSocketListener(alarmEngine::acknowledgeAlarm, WebSocketListener.Configuration.fromMap(config));                
            default:
                throw new IllegalArgumentException("Unknown dispatcher type: " + listenerType);
        }
    }
}