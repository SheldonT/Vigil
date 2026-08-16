# Components

## Monitors

A monitor polls a single system metric and feeds readings to the alarm engine and telemetry tracker every poll cycle.

### Built-in monitors

| Type               | Config key          | Metric                                            |
|--------------------|---------------------|---------------------------------------------------|
| `CPU`              | `monitor.CPU`       | System-wide CPU usage (%)                         |
| `Memory`           | `monitor.Memory`    | System memory usage (%)                           |
| `ProcessCpuUsage`  | `monitor.ProcessCpuUsage` | CPU usage of the Vigil JVM process (%)      |
| `SystemLoadAverage`| `monitor.SystemLoadAverage` | OS 1-minute load average                  |

### Monitor config options

```toml
[monitor.CPU]
enabled           = true
type              = "CPU"
telemetryDeadband = 0.5   # minimum change to trigger a telemetry dispatch
```

`telemetryDeadband` suppresses noise: a telemetry event is only dispatched when the reading has changed by more than this value since the last dispatched reading.

---

## Listeners

Listeners receive inbound messages from clients. Currently the only supported inbound message type is `ACKNOWLEDGE_ALARM`.

### Built-in listeners

| Type        | Config key          | Transport          | Description                                  |
|-------------|---------------------|--------------------|----------------------------------------------|
| `MQTT`      | `listener.MQTT`     | MQTT 5             | Subscribes to a topic on a Mosquitto broker  |
| `WebSocket` | `listener.WebSocket`| WebSocket (WS)     | Connects to a WebSocket relay server as a client |

### MQTT listener config

```toml
[listener.MQTT]
type  = "MQTT"
host  = "localhost"
port  = 1883
topic = "client/vigil"
```

### WebSocket listener config

```toml
[listener.WebSocket]
type = "WebSocket"
host = "ws://localhost:8080"
```

The WebSocket listener connects to the relay server as a **client**. Any message broadcast by the relay that has `"type": "ACKNOWLEDGE_ALARM"` will be processed.

---

## Dispatchers

Dispatchers send `ALARM`, `TELEMETRY`, and `ALARM_ACKNOWLEDGED` events outbound to configured destinations.

### Built-in dispatchers

| Type        | Config key           | Transport          | Description                                     |
|-------------|----------------------|--------------------|-------------------------------------------------|
| `File`      | `dispatcher.File`    | Local file         | Writes human-readable event lines to a rotating flat file |
| `MQTT`      | `dispatcher.MQTT`    | MQTT 5             | Publishes JSON messages to a topic              |
| `WebSocket` | `dispatcher.WebSocket`| WebSocket (WS)    | Sends JSON messages to a WebSocket relay server |

### File dispatcher config

```toml
[dispatcher.File]
type         = "File"
fileName     = "event_output"
maxLineCount = 1000     # rotate to a new file after this many lines
```

Output format example:
```
STATUS    => 2026-08-15T17:41:08Z - CPU - c6d1919b-... HIGH_ALARM : 92.4
TELEMETRY => 2026-08-15T17:41:09Z - CPU : 14.3
ALARM ACK => 2026-08-15T17:44:10Z - CPU - c6d1919b-... - ACK
```

### MQTT dispatcher config

```toml
[dispatcher.MQTT]
type  = "MQTT"
host  = "localhost"
port  = 1883
topic = "client/vigil"
```

Publishes JSON to the configured topic. All three message types (`ALARM`, `TELEMETRY`, `ALARM_ACKNOWLEDGED`) are published to the same topic.

### WebSocket dispatcher config

```toml
[dispatcher.WebSocket]
type = "WebSocket"
host = "ws://localhost:8080"
```

Connects to the relay as a client and sends JSON. The relay is responsible for broadcasting to subscribed clients.
