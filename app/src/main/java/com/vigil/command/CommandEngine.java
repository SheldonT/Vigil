package com.vigil.command;

import java.util.function.Consumer;
import java.util.Map;
import java.util.HashMap;

import com.vigil.alarm.AlarmEngine;

public class CommandEngine {
    
    private final Map<CommandType, Consumer<VigilCommand>> commands = new HashMap<>();

    public CommandEngine (AlarmEngine alarmEngine){

        this.commands.put(
            CommandType.ACKNOWLEDGE,
            command -> {
                AlarmAcknowledgeIn acknowledge =
                    (AlarmAcknowledgeIn) command;

                alarmEngine.acknowledgeAlarm(
                    acknowledge.alarmId()
                );
            }
        );
    }

    public void handleCommand(VigilCommand command){
        Consumer<VigilCommand> handler = this.commands.get(command.commandType());

        if (handler == null){
            throw new IllegalArgumentException(
                "Unsupported Command " + command.commandType()
            );
        }

        handler.accept(command);
    }

}