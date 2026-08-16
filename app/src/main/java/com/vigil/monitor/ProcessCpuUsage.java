package com.vigil.monitor;

import java.time.Instant;
import java.util.Map;

import com.vigil.alarm.AlarmEvaluator;
import com.vigil.config.ConfigValidator;

public class ProcessCpuUsage extends Monitor<Double>{

    public record Configuration(String type, boolean enabled, double telemetryDeadband) implements MonitorConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, "CPU Monitor Map");
            Configuration config = new Configuration(
                ConfigValidator.requireString(validMap, "CPU Monitor", "type"),
                ConfigValidator.requireBool(validMap, "CPU Monitor", "enabled"),
                ConfigValidator.requireDouble(validMap, "CPU Monitor", "telemetryDeadband")
            );

            return config;
        }

        @Override
        public String getType(){
            return "CPU";
        }
    }

    private final SystemMetricsProvider metrics;
    private final Configuration config;
    private final AlarmEvaluator<Double> evaluator;

    public ProcessCpuUsage(SystemMetricsProvider metrics, AlarmEvaluator<Double> evaluator, Configuration config) {
        super("ProcessCpuUsage");

        this.config = config;
        this.metrics = metrics;
        this.evaluator = evaluator;
    }

    @Override
    public TelemetryOut<Double> read(){
        return new TelemetryOut<Double>(
            this.getName(),
            metrics.processCpuUsage(),
            Instant.now()
        );
    }

    @Override
    public double getTelemetryDeadband() {
        return this.config.telemetryDeadband();
    }

    @Override
    public AlarmEvaluator<Double> getAlarmEvaluator() {
        return this.evaluator;
    }
}
