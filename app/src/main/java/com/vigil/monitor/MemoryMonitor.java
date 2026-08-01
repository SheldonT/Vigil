package com.vigil.monitor;

import java.time.Instant;
import java.util.Map;

import com.vigil.alarm.AlarmEvaluator;
import com.vigil.config.ConfigValidator;

public class MemoryMonitor extends Monitor<Double>{

    public record Configuration(String type, boolean enabled, double telemetryDeadband) implements MonitorConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, "CPU Monitor Map");
            Configuration config = new Configuration(
                ConfigValidator.requireString(validMap, "Memory Monitor", "type"),
                ConfigValidator.requireBool(validMap, "Memory Monitor", "enabled"),
                ConfigValidator.requireDouble(validMap, "Memory Monitor", "telemetryDeadband")
            );

            return config;
        }

        @Override
        public String getType(){
            return "Memory";
        }
    }

    private final SystemMetricsProvider metrics;
    private final Configuration config;
    private final AlarmEvaluator<Double> evaluator;

    public MemoryMonitor(SystemMetricsProvider metrics, AlarmEvaluator<Double> evaluator, Configuration config) {
        super("Memory");

        this.metrics = metrics;
        this.config = config;
        this.evaluator = evaluator;
    }

    @Override
    public MonitorReading<Double> read(){
        return new MonitorReading<Double>(
            this.getName(),
            metrics.memoryUsage(),
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