package com.vigil.factory;

import java.util.Map;

import com.vigil.dispatcher.Dispatcher;
import com.vigil.dispatcher.FileDispatcher;
import com.vigil.dispatcher.MqttDispatcher;
import com.vigil.dispatcher.WebSocketDispatcher;
import com.vigil.app.AppConfig;

public class DispatcherFactory{

    public static Dispatcher create(Map<String, Object> dispatchConfig, AppConfig appConfig){

        String dispatchType = (String)dispatchConfig.get("type");


        switch(dispatchType){
            case "File":
                return new FileDispatcher(FileDispatcher.Configuration.fromMap(dispatchConfig), appConfig);
            
            case "MQTT":
                return new MqttDispatcher(MqttDispatcher.Configuration.fromMap(dispatchConfig), appConfig);

            case "WebSocket":
                return new WebSocketDispatcher(WebSocketDispatcher.Configuration.fromMap(dispatchConfig), appConfig);
                
            default:
                throw new IllegalArgumentException("Unknown dispatcher type: " + dispatchType);
        }
    }
}