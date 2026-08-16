package com.vigil.listener;

import java.util.UUID;

import com.vigil.message.VigilMessage;

public interface  AlarmAcknowledger{
    VigilMessage acknowledgeAlarm(UUID alarmId);
}