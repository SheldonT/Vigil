package com.vigil.factory;

import java.util.Map;

import com.vigil.dispatcher.Dispatcher;
import com.vigil.dispatcher.FileDispatcher;
import com.vigil.dispatcher.MqttDispatcher;
import com.vigil.dispatcher.WebSocketDispatcher;

public class DispatcherFactory{

    public static Dispatcher create(Map<String, Object> config){

        String dispatchType = (String)config.get("type");


        switch(dispatchType){
            case "File":
                return new FileDispatcher(FileDispatcher.Configuration.fromMap(config));
            
            case "MQTT":
                return new MqttDispatcher(MqttDispatcher.Configuration.fromMap(config));

            case "WebSocket":
                return new WebSocketDispatcher(WebSocketDispatcher.Configuration.fromMap(config));
                
            default:
                throw new IllegalArgumentException("Unknown dispatcher type: " + dispatchType);
        }
    }
}