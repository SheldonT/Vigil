package com.vigil.factory;

import java.util.Map;

import com.vigil.alarm.AlarmEvaluator;
import com.vigil.alarm.NumericAlarmConfig;
import com.vigil.alarm.NumericAlarmEvaluator;

public class AlarmEvaluatorFactory {

    public static AlarmEvaluator<Double> createNumeric (String monitorType, Map<String,Object> alarmTable){

        return new NumericAlarmEvaluator(NumericAlarmConfig.fromMap(monitorType, alarmTable));
    }
}
