package com.vigil.factory;

import java.util.Map;

import com.vigil.dispatcher.Dispatcher;
import com.vigil.dispatcher.FileDispatcher;

public class DispatcherFactory{

    public static Dispatcher create(Map<String, Object> config){

        String dispatchType = (String)config.get("type");


        switch(dispatchType){
            case "File":
                return new FileDispatcher(FileDispatcher.Configuration.fromMap(config));
                
            default:
                throw new IllegalArgumentException("Unknown monitor type: " + dispatchType);
        }
    }
}