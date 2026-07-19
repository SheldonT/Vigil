package com.vigil.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vigil.monitor.Monitor;

class AlarmEngineTest {

    /**
     * A simple controllable Monitor stub — no SystemMetricsProvider needed.
     */
    private static class StubMonitor extends Monitor {
        private double value;

        StubMonitor(String name, double initialValue) {
            super(name);
            this.value = initialValue;
        }

        void setValue(double v) { this.value = v; }

        @Override
        public double get() { return value; }
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
        StubMonitor monitor = new StubMonitor("CPU", 50.0);
        AlarmEngine engine = engineWith(monitor);

        List<AlarmResult> events = engine.evaluate();

        assertTrue(events.isEmpty(), "No event expected when value stays OK");
    }

    // ---- HIGH_WARNING transitions ----

    @Test
    void highWarningEvent_whenValueCrossesHighWarnThreshold() {
        StubMonitor monitor = new StubMonitor("CPU", 50.0);
        AlarmEngine engine = engineWith(monitor);

        monitor.setValue(82.0);   // crosses HIGH_WARN (80)
        List<AlarmResult> events = engine.evaluate();

        assertEquals(1, events.size());
        assertEquals(Status.HIGH_WARNING, events.get(0).status);
    }

    @Test
    void noEvent_whenValueStaysInHighWarningZone() {
        StubMonitor monitor = new StubMonitor("CPU", 82.0);
        AlarmEngine engine = engineWith(monitor);

        engine.evaluate();           // consume the initial HIGH_WARNING event (if any)
        monitor.setValue(83.0);      // still in HIGH_WARNING zone
        List<AlarmResult> events = engine.evaluate();

        assertTrue(events.isEmpty(), "No event expected while value stays in HIGH_WARNING");
    }

    // ---- HIGH_ALARM transitions ----

    @Test
    void highAlarmEvent_whenValueCrossesHighAlarmThreshold() {
        StubMonitor monitor = new StubMonitor("CPU", 82.0);
        AlarmEngine engine = engineWith(monitor);
        engine.evaluate();   // prime engine to HIGH_WARNING state

        monitor.setValue(92.0);   // crosses HIGH_ALARM (90)
        List<AlarmResult> events = engine.evaluate();

        assertEquals(1, events.size());
        assertEquals(Status.HIGH_ALARM, events.get(0).status);
    }

    // ---- LOW_WARNING transitions ----

    @Test
    void lowWarningEvent_whenValueCrossesLowWarnThreshold() {
        StubMonitor monitor = new StubMonitor("CPU", 50.0);
        AlarmEngine engine = engineWith(monitor);

        monitor.setValue(18.0);   // crosses LOW_WARN (20)
        List<AlarmResult> events = engine.evaluate();

        assertEquals(1, events.size());
        assertEquals(Status.LOW_WARNING, events.get(0).status);
    }

    // ---- LOW_ALARM transitions ----

    @Test
    void lowAlarmEvent_whenValueCrossesLowAlarmThreshold() {
        StubMonitor monitor = new StubMonitor("CPU", 18.0);
        AlarmEngine engine = engineWith(monitor);
        engine.evaluate();   // prime engine to LOW_WARNING state

        monitor.setValue(8.0);   // crosses LOW_ALARM (10)
        List<AlarmResult> events = engine.evaluate();

        assertEquals(1, events.size());
        assertEquals(Status.LOW_ALARM, events.get(0).status);
    }

    // ---- clearing back to OK ----

    @Test
    void okEvent_whenHighWarningValueClearsToOk() {
        StubMonitor monitor = new StubMonitor("CPU", 82.0);
        AlarmEngine engine = engineWith(monitor);
        engine.evaluate();   // prime to HIGH_WARNING

        monitor.setValue(50.0);   // drops below HIGH_WARN_CLEAR (75) → OK
        List<AlarmResult> events = engine.evaluate();

        assertEquals(1, events.size());
        assertEquals(Status.OK, events.get(0).status);
    }

    // ---- missing alarm config ----

    @Test
    void constructor_throws_whenAlarmConfigMissingForMonitor() {
        StubMonitor monitor = new StubMonitor("CPU", 50.0);
        assertThrows(IllegalStateException.class,
                () -> new AlarmEngine(List.of(monitor), Map.of()));   // empty config map
    }
}
