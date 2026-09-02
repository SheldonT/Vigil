package com.vigil.alarm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vigil.message.AlarmAcknowledgeOut;
import com.vigil.message.AlarmMessage;
import com.vigil.message.TelemetryOut;
import com.vigil.message.AlarmAcknowledgeFail;
import com.vigil.message.VigilMessage;
import com.vigil.dispatcher.OutgoingMessageQueue;
import com.vigil.monitor.Monitor;

class AlarmEngineTest {

    /**
     * A simple controllable Monitor stub — no SystemMetricsProvider needed.
     */
    private static class StubMonitor extends Monitor<Double> {
        private TelemetryOut<Double> value;
        private final AlarmEvaluator<Double> evaluator;

        StubMonitor(String name, TelemetryOut<Double> initialValue, AlarmEvaluator<Double> evaluator) {
            super(name);
            this.value = initialValue;
            this.evaluator = evaluator;
        }

        void setValue(TelemetryOut<Double> v) { this.value = v; }

        @Override
        public TelemetryOut<Double> read() { return this.value; }

        @Override
        public AlarmEvaluator<Double> getAlarmEvaluator() { return this.evaluator; }
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

    private NumericAlarmConfig config;
    private AlarmEvaluator<Double> evaluator;
    private OutgoingMessageQueue outputMessages;

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
        config = NumericAlarmConfig.fromMap("CPU", map);
        evaluator = new NumericAlarmEvaluator(config);
        outputMessages = new OutgoingMessageQueue();
    }

    private AlarmEngine engineWith(StubMonitor monitor) {
        return new AlarmEngine(List.of(monitor), outputMessages);
    }

    private AlarmMessage<Double> evaluate(AlarmEngine engine, StubMonitor monitor) {
        return engine.evaluate(monitor.read(), monitor.getAlarmEvaluator());
    }

    // ---- initial state ----

    @Test
    void noEvent_whenValueStaysInOkZone() {

        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);

        AlarmMessage<Double> events = evaluate(engine, monitor);

        assertTrue(events == null, "No event expected when value stays OK");
    }

    // ---- HIGH_WARNING transitions ----

    @Test
    void highWarningEvent_whenValueCrossesHighWarnThreshold() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);


        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 82.0, Instant.now());

        monitor.setValue(testValue);   // crosses HIGH_WARN (80)
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertTrue(event != null, "Event expected when crossing high warning");
        assertEquals(Status.HIGH_WARNING, event.status());
    }

    @Test
    void noEvent_whenValueStaysInHighWarningZone() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 82.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);

        evaluate(engine, monitor);           // consume the initial HIGH_WARNING event (if any)
        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 83.0, Instant.now());

        monitor.setValue(testValue);      // still in HIGH_WARNING zone
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertTrue(event == null, "No event expected while value stays in HIGH_WARNING");
    }

    // ---- HIGH_ALARM transitions ----

    @Test
    void highAlarmEvent_whenValueCrossesHighAlarmThreshold() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 82.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);

        evaluate(engine, monitor);   // prime engine to HIGH_WARNING state

        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 92.0, Instant.now());

        monitor.setValue(testValue);   // crosses HIGH_ALARM (90)
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertTrue(event != null, "Event expected while value crossing into HIGH_ALARM");
        assertEquals(Status.HIGH_ALARM, event.status());
    }

    // ---- LOW_WARNING transitions ----

    @Test
    void lowWarningEvent_whenValueCrossesLowWarnThreshold() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);

        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 18.0, Instant.now());

        monitor.setValue(testValue);   // crosses LOW_WARN (20)
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertTrue(event != null, "Event expected while value crossing below LOW_WARNING");
        assertEquals(Status.LOW_WARNING, event.status());
    }

    // ---- LOW_ALARM transitions ----

    @Test
    void lowAlarmEvent_whenValueCrossesLowAlarmThreshold() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 18.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);
        evaluate(engine, monitor);   // prime engine to LOW_WARNING state

        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 8.0, Instant.now());

        monitor.setValue(testValue);   // crosses LOW_ALARM (10)
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertTrue(event != null, "Event expected while value crossing below LOW_ALARM");
        assertEquals(Status.LOW_ALARM, event.status());
    }

    // ---- clearing back to OK ----

    @Test
    void okEvent_whenHighWarningValueClearsToOk() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 82.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);
        evaluate(engine, monitor);   // prime to HIGH_WARNING

        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());


        monitor.setValue(testValue);   // drops below HIGH_WARN_CLEAR (75) → OK
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertTrue(event != null, "Event expected while value crossing from ALARM_HIGH to OK");
        assertEquals(Status.OK, event.status());
    }

    // ---- constructor behavior ----

    @Test
    void constructor_initializesEngineWithMonitorEvaluator() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        assertDoesNotThrow(() -> new AlarmEngine(List.of(monitor), outputMessages));
    }

    @Test
    void acknowledgeAlarm_enqueuesAcknowledgementForActiveAlarm() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);

        TelemetryOut<Double> testValue = new TelemetryOut<Double>("CPU", 8.0, Instant.now());
        monitor.setValue(testValue);
        AlarmMessage<Double> event = evaluate(engine, monitor);

        assertNotNull(event, "Expected an active alarm before acknowledging");

        VigilMessage acknowledged = engine.acknowledgeAlarm(event.alarmId());
        VigilMessage outboundAcknowledgement = outputMessages.poll();
        VigilMessage outboundAlarm = outputMessages.poll();

        assertNotNull(acknowledged, "Expected acknowledgement to return alarm state");
        assertInstanceOf(AlarmMessage.class, acknowledged, "Expected acknowledgement to return AlarmMessage");
        AlarmMessage<?> acknowledgedAlarm = (AlarmMessage<?>) acknowledged;
        assertTrue(acknowledgedAlarm.acknowledged(), "Alarm should be marked acknowledged");
        assertNotNull(acknowledgedAlarm.acknowledgedAt(), "Alarm should have acknowledged timestamp");
        assertInstanceOf(AlarmAcknowledgeOut.class, outboundAcknowledgement, "Expected outbound acknowledgement event");
        AlarmAcknowledgeOut outboundAck = (AlarmAcknowledgeOut) outboundAcknowledgement;
        assertEquals(event.alarmId(), outboundAck.alarmId());
        assertEquals("CPU", outboundAck.source());
        assertSame(acknowledged, outboundAlarm, "Expected the acknowledged alarm state to be dispatched");
    }

    @Test
    void acknowledgeAlarm_returnsFailureMessageForUnknownAlarmId() {
        TelemetryOut<Double> initialValue = new TelemetryOut<Double>("CPU", 50.0, Instant.now());
        StubMonitor monitor = new StubMonitor("CPU", initialValue, evaluator);
        AlarmEngine engine = engineWith(monitor);

        UUID unknownAlarmId = UUID.randomUUID();
        VigilMessage acknowledged = engine.acknowledgeAlarm(unknownAlarmId);
    VigilMessage outbound = outputMessages.poll();

        assertInstanceOf(AlarmAcknowledgeFail.class, acknowledged, "Unknown alarm id should return failure message");
    assertInstanceOf(AlarmAcknowledgeFail.class, outbound, "Unknown alarm id should dispatch a failure event");
    AlarmAcknowledgeFail outboundFailure = (AlarmAcknowledgeFail) outbound;
        assertEquals(unknownAlarmId, outboundFailure.alarmId());
    assertNull(outputMessages.poll(), "Unknown alarm id should not dispatch additional messages");
    }

    @Test
    void acknowledgeIncomingMessage_acceptsDeviceMetadata() {
        String json = """
            {
              "type": "COMMAND",
              "commandType": "ACKNOWLEDGE",
                            "payload": {
                                "alarmId": "c6d1919b-5553-4fa9-823c-c74aa4b71511",
                                "deviceId": "nas-01",
                                "deviceName": "NAS"
                            }
            }
            """;

        var listener = new com.vigil.listener.Listener() {
            @Override
            public void start() {}

            @Override
            public void stop() {}

            public com.vigil.command.VigilCommand parse(String payload) {
                return deserialize(payload).orElseThrow();
            }
        };

        com.vigil.command.VigilCommand message = listener.parse(json);
        assertEquals(com.vigil.command.CommandType.ACKNOWLEDGE, message.commandType());
        assertInstanceOf(com.vigil.message.CommandMessage.class, message);

        com.vigil.message.CommandMessage command =
            (com.vigil.message.CommandMessage) message;
        assertEquals(
            "c6d1919b-5553-4fa9-823c-c74aa4b71511",
            command.payload().get("alarmId").asText()
        );
        assertEquals("nas-01", command.payload().get("deviceId").asText());
        assertEquals("NAS", command.payload().get("deviceName").asText());
    }
}
