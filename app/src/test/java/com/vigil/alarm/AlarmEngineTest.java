package com.vigil.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vigil.monitor.Monitor;
import com.vigil.monitor.MonitorReading;

class AlarmEngineTest {

    /**
     * A simple controllable Monitor stub — no SystemMetricsProvider needed.
     */
    private static class StubMonitor extends Monitor<Double> {
        private MonitorReading<Double> value;

        StubMonitor(String name, MonitorReading<Double> initialValue) {
            super(name);
            this.value = initialValue;
        }

        void setValue(MonitorReading<Double> v) { this.value = v; }

        @Override
        public MonitorReading<Double> read() { return this.value; }
    }

    // Setpoints used across all tests:
    //   LOW_ALARM  <= 10    (clear >= 15)
    //   LOW_WARN   <= 20    (clear >= 25)
    //   OK         (25 – 75)
    //   HIGH_WARN  >= 80    (clear <= 75)
    //   HIGH_ALARM >= 90    (clear <= 85)
    private static final double HIGH_ALARM        = 90.0;
    private static final double HIGH_ALARM_CLEAR  = 85.0;
    private static final double HIGH_WARN         = 80.0;
    private static final double HIGH_WARN_CLEAR   = 75.0;
    private static final double LOW_WARN          = 20.0;
    private static final double LOW_WARN_CLEAR    = 25.0;
    private static final double LOW_ALARM         = 10.0;
    private static final double LOW_ALARM_CLEAR   = 15.0;

    private AlarmConfig config;

    @BeforeEach
    void buildConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("highAlarm",         HIGH_ALARM);
        map.put("highAlarmClear",    HIGH_ALARM_CLEAR);
        map.put("highWarning",       HIGH_WARN);
        map.put("highWarningClear",  HIGH_WARN_CLEAR);
        map.put("lowWarning",        LOW_WARN);
        map.put("lowWarningClear",   LOW_WARN_CLEAR);
        map.put("lowAlarm",          LOW_ALARM);
        map.put("lowAlarmClear",     LOW_ALARM_CLEAR);
        map.put("activationDelayMs", 0L);   // no delay – transitions are instant
        map.put("clearDelayMs",      0L);
        config = AlarmConfig.fromMap("CPU", map);
    }

    private AlarmEngine engineWith(StubMonitor monitor) {
        return new AlarmEngine(List.of(monitor), Map.of("CPU", config));
    }

    // ---- initial state ----

    @Test
    void noEvent_whenValueStaysInOkZone() {

        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);

        AlarmResult events = engine.evaluate(monitor.read());

        assertTrue(events == null, "No event expected when value stays OK");
    }

    // ---- HIGH_WARNING transitions ----

    @Test
    void highWarningEvent_whenValueCrossesHighWarnThreshold() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);


        MonitorReading<Double> testValue = new MonitorReading<Double>("CPU", 82.0, Instant.now());

        monitor.setValue(testValue);   // crosses HIGH_WARN (80)
        AlarmResult event = engine.evaluate(monitor.read());

        assertTrue(event != null, "Event expected when crossing high warning");
        assertEquals(Status.HIGH_WARNING, event.status);
    }

    @Test
    void noEvent_whenValueStaysInHighWarningZone() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 82.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);

        engine.evaluate(monitor.read());           // consume the initial HIGH_WARNING event (if any)
        MonitorReading<Double> testValue = new MonitorReading<Double>("CPU", 83.0, Instant.now());

        monitor.setValue(testValue);      // still in HIGH_WARNING zone
        AlarmResult event = engine.evaluate(monitor.read());

        assertTrue(event == null, "No event expected while value stays in HIGH_WARNING");
    }

    // ---- HIGH_ALARM transitions ----

    @Test
    void highAlarmEvent_whenValueCrossesHighAlarmThreshold() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 82.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);

        engine.evaluate(monitor.read());   // prime engine to HIGH_WARNING state

        MonitorReading<Double> testValue = new MonitorReading<Double>("CPU", 92.0, Instant.now());

        monitor.setValue(testValue);   // crosses HIGH_ALARM (90)
        AlarmResult event = engine.evaluate(monitor.read());

        assertTrue(event != null, "Event expected while value crossing into HIGH_ALARM");
        assertEquals(Status.HIGH_ALARM, event.status);
    }

    // ---- LOW_WARNING transitions ----

    @Test
    void lowWarningEvent_whenValueCrossesLowWarnThreshold() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);

        MonitorReading<Double> testValue = new MonitorReading<Double>("CPU", 18.0, Instant.now());

        monitor.setValue(testValue);   // crosses LOW_WARN (20)
        AlarmResult event = engine.evaluate(monitor.read());

        assertTrue(event != null, "Event expected while value crossing below LOW_WARNING");
        assertEquals(Status.LOW_WARNING, event.status);
    }

    // ---- LOW_ALARM transitions ----

    @Test
    void lowAlarmEvent_whenValueCrossesLowAlarmThreshold() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 18.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);
        engine.evaluate(monitor.read());   // prime engine to LOW_WARNING state

        MonitorReading<Double> testValue = new MonitorReading<Double>("CPU", 8.0, Instant.now());

        monitor.setValue(testValue);   // crosses LOW_ALARM (10)
        AlarmResult event = engine.evaluate(monitor.read());

        assertTrue(event != null, "Event expected while value crossing below LOW_ALARM");
        assertEquals(Status.LOW_ALARM, event.status);
    }

    // ---- clearing back to OK ----

    @Test
    void okEvent_whenHighWarningValueClearsToOk() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 82.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        AlarmEngine engine = engineWith(monitor);
        engine.evaluate(monitor.read());   // prime to HIGH_WARNING

        MonitorReading<Double> testValue = new MonitorReading<Double>("CPU", 50.0, Instant.now());


        monitor.setValue(testValue);   // drops below HIGH_WARN_CLEAR (75) → OK
        AlarmResult event = engine.evaluate(monitor.read());

        assertTrue(event != null, "Event expected while value crossing from ALARM_HIGH to OK");
        assertEquals(Status.OK, event.status);
    }

    // ---- missing alarm config ----

    @Test
    void constructor_throws_whenAlarmConfigMissingForMonitor() {
        MonitorReading<Double> initialValue = new MonitorReading<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue);
        assertThrows(IllegalStateException.class,
                () -> new AlarmEngine(List.of(monitor), Map.of()));   // empty config map
    }
}
