# Vigil

A lightweight, configurable system monitoring and alerting tool written in Java. Vigil polls system metrics, evaluates them against configurable alarm setpoints, and dispatches events when thresholds are crossed.

## Features

- **Built-in monitors** for CPU usage, memory usage, process CPU usage, and system load average
- **Alarm engine** with high/low warning and alarm levels, configurable hysteresis (clear setpoints), and activation/clear delays to prevent flapping
- **Extensible** — add custom monitors and dispatchers with minimal boilerplate
- **TOML-based configuration**
- **Configurable logging** via the JVM logging framework

## Requirements

- Java 21+
- Gradle (or use the included `gradlew` wrapper)

## Download

The latest official release of Vigil is available here:

[Download Vigil](https://github.com/SheldonT/Vigil/releases/latest)

Download the `.jar` file from the release assets and run it with Java 21 or newer.

## Building

```bash
./gradlew shadowJar
```

This produces a self-contained fat JAR at `app/build/libs/vigil.jar`.

## Running

```bash
java -jar app/build/libs/vigil.jar path/to/config.toml
```

## Configuration

All configuration is done in a single TOML file. A complete example:

```toml
[logging]
fileName    = "vigil.log"
fileSize    = 512        # MB per log file
fileCount   = 10         # number of rotating log files
toOSConsole = false

[dispatcher.File]
type         = "File"
fileName     = "event_output"
maxLineCount = 1000      # rotate output file after this many lines

[monitor.CPU]
type = "CPU"

[monitor.CPU.alarm]
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

### Monitors

| Type               | Metric                                      |
|--------------------|---------------------------------------------|
| `CPU`              | System-wide CPU usage (%)                   |
| `Memory`           | System memory usage (%)                     |
| `ProcessCpuUsage`  | CPU usage of the Vigil process itself (%)   |
| `SystemLoadAverage`| OS system load average                      |

### Alarm setpoints

Each monitor requires an `[monitor.<name>.alarm]` section.

| Key                | Description                                                     |
|--------------------|-----------------------------------------------------------------|
| `highAlarm`        | Value at or above which a HIGH_ALARM event fires                |
| `highAlarmClear`   | Value must drop to or below this to clear a HIGH_ALARM          |
| `highWarning`      | Value at or above which a HIGH_WARNING event fires              |
| `highWarningClear` | Value must drop to or below this to clear a HIGH_WARNING        |
| `lowWarning`       | Value at or below which a LOW_WARNING event fires               |
| `lowWarningClear`  | Value must rise to or above this to clear a LOW_WARNING         |
| `lowAlarm`         | Value at or below which a LOW_ALARM event fires                 |
| `lowAlarmClear`    | Value must rise to or above this to clear a LOW_ALARM           |
| `activationDelayMs`| Milliseconds a threshold must be exceeded before the alarm fires|
| `clearDelayMs`     | Milliseconds a clear condition must hold before the alarm clears|

Setpoints must satisfy: `lowAlarm < lowWarning < highWarning < highAlarm`.

### Dispatchers

| Type   | Description                                    |
|--------|------------------------------------------------|
| `File` | Writes alarm events to a rotating flat file    |

## Extending Vigil

### Adding a custom monitor

1. Create a class that extends `Monitor`:

```java
package com.vigil.monitor;

public class DiskMonitor extends Monitor {

    private final SystemMetricsProvider metrics;

    public DiskMonitor(SystemMetricsProvider metrics) {
        super("Disk");
        this.metrics = metrics;
    }

    @Override
    public double get() {
        return metrics.diskUsage(); // implement in SystemMetricsProvider
    }
}
```

2. Register it in `MonitorFactory`:

```java
case "Disk":
    return new DiskMonitor(provider);
```

3. Add a corresponding `[monitor.Disk]` and `[monitor.Disk.alarm]` section to your config.

### Adding a custom dispatcher

1. Create a class that extends `Dispatcher`:

```java
package com.vigil.dispatcher;

import com.vigil.alarm.AlarmResult;

public class ConsoleDispatcher extends Dispatcher {

    @Override
    public void send(AlarmResult result) {
        System.out.println(result.timestampNow + " " + result.name + " " + result.status);
    }
}
```

2. Register it in `DispatcherFactory`:

```java
case "Console":
    return new ConsoleDispatcher();
```

3. Add a `[dispatcher.Console]` section with `type = "Console"` to your config.

## Running tests

```bash
./gradlew test
```

## License

[MIT](LICENSE)
