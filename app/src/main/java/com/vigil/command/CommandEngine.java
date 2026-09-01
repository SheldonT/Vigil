package com.vigil.command;

import java.util.function.Consumer;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigil.alarm.AlarmEngine;
import com.vigil.alarm.MonitorState;
import com.vigil.alarm.AlarmState;
import com.vigil.dispatcher.OutgoingEventSink;
import com.vigil.message.TelemetryOut;
import com.vigil.message.AlarmMessage;

public class CommandEngine {
    
    private final Map<CommandType, Consumer<JsonNode>> commands = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OutgoingEventSink eventSink;

    public CommandEngine (AlarmEngine alarmEngine, OutgoingEventSink eventSink){

        this.eventSink = eventSink;

        this.commands.put(
            CommandType.ACKNOWLEDGE,
            payload -> {
                AlarmAcknowledgePayload command =
                    objectMapper.convertValue(payload, AlarmAcknowledgePayload.class);

                alarmEngine.acknowledgeAlarm(command.alarmId());
            }
        );
        this.commands.put(
            CommandType.GET_STATE,
            payload -> {
                for (MonitorState<?> mStates : alarmEngine.getMonitorStates().values()){
                    TelemetryOut<?> currenTelemetryOut = mStates.toMessage();

                    this.eventSink.submit(currenTelemetryOut);
                }

                for (AlarmState<?> aStates : alarmEngine.getAlarmStates().values()){
                    AlarmMessage<?> currenAlarmOut = aStates.toMessage();

                    this.eventSink.submit(currenAlarmOut);
                }
            } 
        );
    }

    public void handleCommand(VigilCommand command){
        Consumer<JsonNode> handler = this.commands.get(command.commandType());

        if (handler == null){
            throw new IllegalArgumentException(
                "Unsupported Command " + command.commandType()
            );
        }

        handler.accept(command.payload());
    }

}