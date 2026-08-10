package com.vigil.listener;

import java.util.UUID;

import com.vigil.alarm.AlarmMessage;

public interface  AlarmAcknowledger{
    AlarmMessage<?> acknowledgeAlarm(UUID alarmId);
}