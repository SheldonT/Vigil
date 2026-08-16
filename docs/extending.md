# Extending Vigil

## Adding a custom monitor

### 1. Implement `Monitor<T>`

Create a class in `com.vigil.monitor` that extends `Monitor<T>`. The type parameter `T` is the reading value type (typically `Double`).

```java
package com.vigil.monitor;

public class DiskMonitor extends Monitor<Double> {

    private final SystemMetricsProvider metrics;
    private final AlarmEvaluator<Double> evaluator;
    private final double deadband;

    public DiskMonitor(
        SystemMetricsProvider metrics,
        AlarmEvaluator<Double> evaluator,
        double deadband
    ) {
        super("Disk");
        this.metrics   = metrics;
        this.evaluator = evaluator;
        this.deadband  = deadband;
    }

    @Override
    public TelemetryOut<Double> read() {
        return new TelemetryOut<>("Disk", metrics.diskUsage(), Instant.now());
    }

    @Override
    public AlarmEvaluator<Double> getAlarmEvaluator() {
        return this.evaluator;
    }

    @Override
    public double getTelemetryDeadband() {
        return this.deadband;
    }
}
```

Add the metric method to `SystemMetricsProvider` and implement it in `JvmSystemMetricsProvider`.

### 2. Add a `Configuration` record (optional)

If the monitor needs config from the TOML file, add a static inner `Configuration` record with a `fromMap` factory, following the pattern in `CpuMonitor`.

### 3. Register in `MonitorFactory`

```java
case "Disk": {
    AlarmEvaluator<Double> evaluator = AlarmEvaluatorFactory.createNumeric(type, alarmTable);
    return new DiskMonitor(provider, evaluator, /* deadband from map */);
}
```

### 4. Add to config

```toml
[monitor.Disk]
type              = "Disk"
enabled           = true
telemetryDeadband = 1.0

[monitor.Disk.alarm]
type              = "numeric"
highWarning       = 80.0
highWarningClear  = 79.0
highAlarm         = 90.0
highAlarmClear    = 89.0
lowWarning        = 0.0
lowWarningClear   = 0.0
lowAlarm          = 0.0
lowAlarmClear     = 0.0
activationDelayMs = 2000
clearDelayMs      = 2000
```

---

## Adding a custom dispatcher

### 1. Implement `Dispatcher`

Create a class in `com.vigil.dispatcher` that extends `Dispatcher`. Use `serialize(message)` from the base class to convert any `VigilMessage` to JSON.

```java
package com.vigil.dispatcher;

import com.vigil.alarm.AlarmAcknowledgeOut;
import com.vigil.alarm.AlarmMessage;
import com.vigil.monitor.TelemetryOut;

public class HttpDispatcher extends Dispatcher {

    private final String endpoint;

    public HttpDispatcher(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void start() { /* open connection / initialise client */ }

    @Override
    public void stop() { /* close connection */ }

    @Override
    public void sendAlarm(AlarmMessage<?> result) {
        post(serialize(result));
    }

    @Override
    public void sendValue(TelemetryOut<?> value) {
        post(serialize(value));
    }

    @Override
    public void sendAlarmAcknowledgement(AlarmAcknowledgeOut acknowledgement) {
        post(serialize(acknowledgement));
    }

    private void post(String json) {
        // send HTTP POST with json as body
    }
}
```

The `serialize()` method produces the standard Vigil JSON payload including the `"type"` field.

### 2. Add a `Configuration` record

Add a static inner `Configuration` record with a `fromMap(Map<String, Object>)` factory, following the pattern in `FileDispatcher` or `MqttDispatcher`.

### 3. Register in `DispatcherFactory`

```java
case "Http":
    return new HttpDispatcher(HttpDispatcher.Configuration.fromMap(config));
```

### 4. Add to config

```toml
[dispatcher.Http]
type     = "Http"
endpoint = "https://my-server.example.com/vigil/events"
```

---

## Adding a custom listener

### 1. Implement `Listener`

Create a class in `com.vigil.listener` that extends `Listener` and implements `AlarmAcknowledger`.

- Call `deserialize(rawJson)` to parse inbound messages — it returns `Optional<VigilMessage>`, which is non-empty only for actionable message types (currently `ACKNOWLEDGE_ALARM`).
- Call `ifPresent(this::handleMessage)` to route to `handleMessage`.
- Provide a `Function<UUID, AlarmMessage<?>>` callback in the constructor so the listener can trigger acknowledgements on the engine.

```java
package com.vigil.listener;

import java.util.UUID;
import java.util.function.Function;
import com.vigil.alarm.AlarmAcknowledgeIn;
import com.vigil.alarm.AlarmMessage;
import com.vigil.app.VigilMessage;

public class HttpPollingListener extends Listener implements AlarmAcknowledger {

    private final Function<UUID, AlarmMessage<?>> ackCallback;
    private volatile boolean running;

    public HttpPollingListener(Function<UUID, AlarmMessage<?>> ackCallback) {
        this.ackCallback = ackCallback;
    }

    @Override
    public void start() {
        running = true;
        // start polling thread / HTTP server / etc.
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    protected void handleMessage(VigilMessage msg) {
        if (msg.type() == com.vigil.app.MessageType.ACKNOWLEDGE_ALARM) {
            AlarmAcknowledgeIn ack = (AlarmAcknowledgeIn) msg;
            acknowledgeAlarm(ack.alarmId());
        }
    }

    @Override
    public AlarmMessage<?> acknowledgeAlarm(UUID alarmId) {
        return ackCallback.apply(alarmId);
    }

    // When a raw JSON string arrives from any transport:
    private void onRawMessage(String json) {
        deserialize(json).ifPresent(this::handleMessage);
    }
}
```

### 2. Register in `ListenerFactory`

```java
case "Http":
    return new HttpPollingListener(alarmEngine::acknowledgeAlarm);
```

### 3. Add to config

```toml
[listener.Http]
type = "Http"
```
