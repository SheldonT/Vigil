# Messages

All Vigil messages are JSON objects with a `"type"` field that identifies the message type.

## Message types

| Type                | Direction        | Description                                                  |
|---------------------|------------------|--------------------------------------------------------------|
| `ALARM`             | Vigil → clients  | An alarm state changed (activated, escalated, or cleared)    |
| `TELEMETRY`         | Vigil → clients  | A monitor reading that crossed the telemetry deadband        |
| `ACKNOWLEDGE_ALARM` | client → Vigil   | A client requests acknowledgement of an active alarm         |
| `ALARM_ACKNOWLEDGED`| Vigil → clients  | Vigil confirms an alarm was acknowledged                     |
| `ALARM_ACKNOWLEDGE_FAILED`| Vigil → clients | Vigil rejected an acknowledge request                     |

---

## ALARM

Dispatched whenever a monitor's alarm status changes — including transitions between warning/alarm levels and clearing back to OK.

```json
{
  "alarmId":       "c6d1919b-5553-4fa9-823c-c74aa4b71511",
  "name":          "CPU",
  "value":         92.4,
  "status":        "HIGH_ALARM",
  "acknowledged":  false,
  "activatedAt":   "2026-08-15T17:43:22.733768153Z",
  "acknowledgedAt": null,
  "lastUpdated":   "2026-08-15T17:43:22.733768153Z",
  "type":          "ALARM"
}
```

| Field           | Description                                                             |
|-----------------|-------------------------------------------------------------------------|
| `alarmId`       | UUID identifying this alarm instance. Use this to acknowledge the alarm.|
| `name`          | Monitor name that raised the alarm                                      |
| `value`         | The reading at the time of the status change                            |
| `status`        | One of `HIGH_ALARM`, `HIGH_WARNING`, `LOW_WARNING`, `LOW_ALARM`, `OK`  |
| `acknowledged`  | Whether the alarm has been acknowledged                                 |
| `activatedAt`   | When the alarm first entered a non-OK state                             |
| `acknowledgedAt`| Timestamp of acknowledgement, or `null` if not yet acknowledged         |
| `lastUpdated`   | Timestamp of this status change                                         |

---

## TELEMETRY

Dispatched when a monitor reading changes beyond the configured `telemetryDeadband`.

```json
{
  "name":      "CPU",
  "value":     14.3,
  "timestamp": "2026-08-15T17:41:08.689907848Z",
  "type":      "TELEMETRY"
}
```

---

## Acknowledging an alarm

To acknowledge an active alarm, send an `ACKNOWLEDGE_ALARM` message to any configured listener (MQTT topic or WebSocket relay):

```json
{
  "type":    "ACKNOWLEDGE_ALARM",
  "alarmId": "c6d1919b-5553-4fa9-823c-c74aa4b71511"
}
```

- `alarmId` must match an alarm that is still in an active (non-OK) state.
- Acknowledging an alarm that has already cleared (or never existed) returns an `ALARM_ACKNOWLEDGE_FAILED` response.
- The `alarmId` is provided in every outgoing `ALARM` message.

---

## ALARM_ACKNOWLEDGED

After a successful acknowledgement, Vigil dispatches this message to all configured dispatchers:

```json
{
  "alarmId":        "c6d1919b-5553-4fa9-823c-c74aa4b71511",
  "acknowledgedAt": "2026-08-15T17:44:10.123456789Z",
  "source":         "CPU",
  "type":           "ALARM_ACKNOWLEDGED"
}
```

| Field            | Description                                           |
|------------------|-------------------------------------------------------|
| `alarmId`        | UUID of the alarm that was acknowledged               |
| `acknowledgedAt` | Timestamp when Vigil processed the acknowledgement    |
| `source`         | Monitor name the alarm belonged to                    |

This message is dispatched to all configured dispatchers, so it appears on MQTT, WebSocket, and file output.

---

## ALARM_ACKNOWLEDGE_FAILED

If an acknowledgement cannot be applied (for example, stale or unknown `alarmId`), Vigil dispatches:

```json
{
  "alarmId": "11111111-2222-3333-4444-555555555555",
  "reason": "Alarm doesn't exist or already acknowledged",
  "type": "ALARM_ACKNOWLEDGE_FAILED"
}
```

| Field      | Description                                                |
|------------|------------------------------------------------------------|
| `alarmId`  | UUID from the request                                      |
| `reason`   | Human-readable failure explanation                         |

This message is also dispatched to all configured dispatchers.

---

## Acknowledgement flow

```
client                  WebSocket/MQTT relay         Vigil listener        AlarmEngine           dispatchers
  |                            |                           |                     |                     |
  |-- ACKNOWLEDGE_ALARM ------>|                           |                     |                     |
  |                            |-- broadcast ------------->|                     |                     |
  |                            |                           |-- acknowledgeAlarm->|                     |
  |                            |                           |                     |-- enqueue success -->|
  |                            |                           |                     |                     |
  |                            |<-- ALARM_ACKNOWLEDGED (if success) ------------------<--------|
  |                            |<-- ALARM_ACKNOWLEDGE_FAILED (if rejected) -----------<--------|
```

After Vigil processes an acknowledgement request, it emits either `ALARM_ACKNOWLEDGED` or `ALARM_ACKNOWLEDGE_FAILED`, typically within one poll cycle (`pollingIntervalMs`).
