# Deployment

## Requirements

- Java 21+
- A config TOML file (see configuration below)
- Any external broker or relay services the configured dispatchers/listeners depend on (Mosquitto, WebSocket relay, etc.)

## Building

Run the pre-deploy check and build in one step:

```bash
./gradlew clean test :app:shadowJar
```

This runs the full test suite and, if all tests pass, produces the fat JAR at:

```
app/build/libs/vigil.jar
```

## Running

```bash
java -jar app/build/libs/vigil.jar path/to/config.toml
```

## Minimum config

The only required sections are `[app]`, `[logging]`, at least one `[monitor.*]`, and at least one `[dispatcher.*]`:

```toml
[app]
pollingIntervalMs = 500

[logging]
fileName    = "vigil.log"
fileSize    = 512
fileCount   = 10
toOSConsole = false

[dispatcher.File]
type         = "File"
fileName     = "event_output"
maxLineCount = 1000

[monitor.CPU]
enabled          = true
type             = "CPU"
telemetryDeadband = 0.5

[monitor.CPU.alarm]
type             = "numeric"
highWarning      = 85.0
highWarningClear = 84.0
highAlarm        = 90.0
highAlarmClear   = 89.0
lowWarning       = 5.0
lowWarningClear  = 6.0
lowAlarm         = 2.0
lowAlarmClear    = 3.0
activationDelayMs = 1000
clearDelayMs      = 1000
```

## Config reference

### `[app]`

| Key               | Type | Description                              |
|-------------------|------|------------------------------------------|
| `pollingIntervalMs` | int | How often monitors are polled (ms)      |

### `[logging]`

| Key           | Type    | Description                                      |
|---------------|---------|--------------------------------------------------|
| `fileName`    | string  | Base name of the rotating log file               |
| `fileSize`    | int     | Max size per log file in KB                      |
| `fileCount`   | int     | Number of rotating log files to keep             |
| `toOSConsole` | boolean | Also print log output to stdout/stderr           |

### `[monitor.<name>]`

| Key               | Type    | Description                                                   |
|-------------------|---------|---------------------------------------------------------------|
| `type`            | string  | Monitor type — see [components.md](components.md)             |
| `enabled`         | boolean | Whether this monitor is active                                |
| `telemetryDeadband` | double | Minimum change before a telemetry event is dispatched       |

### `[monitor.<name>.alarm]`

| Key               | Description                                                              |
|-------------------|--------------------------------------------------------------------------|
| `highAlarm`       | Value at or above which a `HIGH_ALARM` event fires                       |
| `highAlarmClear`  | Value must drop to or below this to clear a `HIGH_ALARM`                 |
| `highWarning`     | Value at or above which a `HIGH_WARNING` event fires                     |
| `highWarningClear`| Value must drop to or below this to clear a `HIGH_WARNING`               |
| `lowWarning`      | Value at or below which a `LOW_WARNING` event fires                      |
| `lowWarningClear` | Value must rise to or above this to clear a `LOW_WARNING`                |
| `lowAlarm`        | Value at or below which a `LOW_ALARM` event fires                        |
| `lowAlarmClear`   | Value must rise to or above this to clear a `LOW_ALARM`                  |
| `activationDelayMs` | How long (ms) a threshold must be exceeded before the alarm activates  |
| `clearDelayMs`    | How long (ms) a clear condition must hold before the alarm clears        |

Setpoints must satisfy: `lowAlarm < lowWarning < highWarning < highAlarm`.