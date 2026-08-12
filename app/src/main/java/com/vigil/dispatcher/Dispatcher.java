package com.vigil.dispatcher;

import java.util.logging.Logger;

import com.vigil.alarm.AlarmAcknowledge;
import com.vigil.alarm.AlarmMessage;
import com.vigil.monitor.MonitorReading;

public abstract class Dispatcher {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    public abstract void sendAlarm(AlarmMessage<?> result);

    public abstract void sendValue(MonitorReading<?> value);

    public abstract void sendAlarmAcknowledgement(AlarmAcknowledge acknowledgement);
}
