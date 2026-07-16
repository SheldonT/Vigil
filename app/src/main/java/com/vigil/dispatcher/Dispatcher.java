package com.vigil.dispatcher;
import com.vigil.alarm.AlarmResult;

public abstract class Dispatcher {

    public abstract void send(AlarmResult result);
}
