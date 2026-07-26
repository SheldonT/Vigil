package com.vigil.dispatcher;

import java.util.logging.Logger;

import com.vigil.alarm.AlarmResult;
import com.vigil.monitor.MonitorReading;

public abstract class Dispatcher {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    public abstract void sendAlarm(AlarmResult result);

    public abstract void sendValue(MonitorReading<?> value);
}
