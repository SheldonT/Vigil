package com.vigil.dispatcher;

import java.util.logging.Logger;

import com.vigil.alarm.AlarmResult;

public abstract class Dispatcher {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    public abstract void send(AlarmResult result);
}
