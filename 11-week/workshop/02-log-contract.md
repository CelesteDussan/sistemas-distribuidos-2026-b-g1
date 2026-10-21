# Structured log contract: absence notification journey

## Scope and encoding

Services `api-gateway`, `attendance-service` and `notifications-service` share this contract.
The domain operation is POST /api/v1/attendance followed by `StudentAbsent` over RabbitMQ.
Use one UTF-8 JSON object per line, no multiline stack traces or unstructured prefixes.
Service names are logical names; event names describe an action, not a person's identity.
Sources: [journey](01-journey.md) and [canonical StudentAbsent](https://github.com/code-corhuila/educk-docs/blob/main/02-domain/domain-events.md#event-studentabsent).

## Common fields

All twelve fields are required on every application log. Unknown duration is zero, not omitted.

| Field | JSON type | Required | Example | Purpose |
|---|---|---|---|---|
| timestamp | string | Yes | 2026-10-21T00:05:00.000Z | UTC ISO-8601 event time |
| level | string enum | Yes | WARN | Severity: ERROR, WARN, INFO or DEBUG |
| service | string | Yes | attendance-service | Emitting logical component |
| version | string | Yes | 1.0.0-workshop | Deployed application version |
| environment | string enum | Yes | dev | Deployment boundary: dev, qa or main |
| trace_id | string, 32 lowercase hex | Yes | 4bf92f3577b34da6a3ce929d0e0e4736 | Cross-service trace lookup; nonzero |
| span_id | string, 16 lowercase hex | Yes | 1111111111111111 | Current operation identity; nonzero |
| correlation_id | UUID string | Yes | bea6076e-cb45-49fa-9df9-b876b7fe01d9 | Stable business correlation across retries |
| event | string | Yes | attendance.outbox.publish_timeout | Stable, bounded machine-readable action |
| message | string | Yes | RabbitMQ publication timed out | Short static text without payload interpolation |
| duration_ms | number, >=0 | Yes | 1000 | Elapsed operation time in milliseconds |

## Domain fields

UUID examples are synthetic. These fields are optional globally and required only in the listed context.
Domain fields are structured values, never metric labels. They may be linkable to a child and remain restricted.

| Field | JSON type | Required context | Example | Purpose |
|---|---|---|---|---|
| attendance_id | UUID string | After AttendanceRecord exists | e079024f-2fdc-43ed-a96b-5105b3bf3ebf | Locate the source record without logging its content |
| event_id | UUID string | Outbox/publication/consumption | 70874170-49f3-4126-93af-19d1f967455b | Check StudentAbsent idempotency and retries |
| session_id | UUID string | Known class session | df80a559-1b49-47c4-9877-78d85ec6e3ea | Correlate the class session without names |
| attendance_status | string enum | Attendance command/event | ABSENT | Distinguish PRESENT, ABSENT, LATE or JUSTIFIED |
| sequence_number | positive integer | StudentAbsent | 15 | Investigate causal ordering and stale deliveries |

The request filter emits the common fields even when no domain context has been validated.
Create a new span for each operation and retain the same trace across HTTP, outbox and AMQP.
A missing/invalid traceparent starts a fresh trace; never accept all-zero trace/span identifiers.
Generate correlation_id when absent or invalid; reuse it in event metadata.correlationId and AMQP headers.
Keep trace context in durable outbox publication metadata; correlation_id alone is not a trace identifier.

## Never log: education and identity data

| Data | Handling before emission |
|---|---|
| Passwords, password hashes, JWTs, cookies, refresh tokens, Authorization | Drop the field completely; no prefix/suffix masking |
| Student/guardian names, emails, phone numbers, identity documents | Drop; an explicitly needed support reference uses keyed HMAC with a protected key |
| Full studentId or teacherId from a request/event | Omit in standard logs; use separately approved pseudonymous reference only when essential |
| Absence reason, medical details, grades, conversation text | Drop the field and its contents; do not dump request/event bodies |
| Database credentials, connection strings, AMQP credentials | Drop; use a fixed dependency name such as RabbitMQ |
| Exception text, raw URLs/query strings or headers containing any of the above | Log an allowlisted error category and route template, never raw values |

Apply this allowlist before serialization, including DEBUG. Restrict log access by operational role,
use environment-specific retention (proposed 7 days dev / 14 days qa / 30 days main), and prohibit
exports with real child data. Retention and access settings are a proposal, not deployed evidence.

## Severity policy

| Level | When | EduTrack example |
|---|---|---|
| ERROR | Technical operation fails and requires recovery | RabbitMQ publish times out; outbox remains pending |
| WARN | Expected rejection or degraded processing worth tracking | Notifications rejects stale StudentAbsent sequence 15 after applying 16 |
| INFO | Significant normal lifecycle boundary | Attendance commits absence and outbox intent |
| DEBUG | Development diagnostics without sensitive payloads | Attendance selects the normalized POST /api/v1/attendance route |

## Seven synthetic JSON log lines

These illustrate one trace, not measured runtime output. HTTP registration succeeds, but asynchronous
notification processing encounters two failures: publisher timeout, then a stale-sequence business rejection.
The publisher retries the same event_id after the timeout; the consumer already has sequence 16.
It rejects sequence 15 without overwriting newer state, acknowledges this terminal stale delivery,
and emits no second notification. The final log confirms that handling, not successful alert delivery.
The higher-sequence event belongs to an earlier independent trace and is not fabricated here.

```jsonl
{"level":"INFO","service":"api-gateway","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:00.000Z","span_id":"1111111111111111","event":"attendance.request.received","message":"Absence registration routed to Attendance","duration_ms":0}
{"level":"INFO","service":"attendance-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:00.018Z","span_id":"2222222222222222","event":"attendance.outbox.committed","message":"Absence and publication intent committed","duration_ms":18,"attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15}
{"level":"ERROR","service":"attendance-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:01.020Z","span_id":"3333333333333333","event":"attendance.outbox.publish_timeout","message":"RabbitMQ publication timed out; outbox remains pending","duration_ms":1000,"attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15}
{"level":"INFO","service":"attendance-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:02.040Z","span_id":"4444444444444444","event":"attendance.outbox.retry_published","message":"StudentAbsent retry published with original event identity","duration_ms":20,"attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15}
{"level":"INFO","service":"notifications-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:02.045Z","span_id":"5555555555555555","event":"notification.event.received","message":"StudentAbsent delivery received","duration_ms":0,"attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15}
{"level":"WARN","service":"notifications-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:02.050Z","span_id":"5555555555555555","event":"notification.sequence.rejected","message":"Stale sequence rejected; newer sequence already applied","duration_ms":5,"attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15}
{"level":"INFO","service":"notifications-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","timestamp":"2026-10-21T00:05:02.052Z","span_id":"5555555555555555","event":"notification.stale_delivery.acknowledged","message":"Stale delivery acknowledged without creating notification","duration_ms":7,"attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15}
```

## Lookup and checks

Filter by `trace_id = "4bf92f3577b34da6a3ce929d0e0e4736"`, order by timestamp, and inspect span_id.
Filter by event_id to investigate redelivery in other traces; do not group unrelated requests by a reused trace.
The sample contains seven objects, twelve common fields each, increasing timestamps, one timeout
and one business rejection. The two notification outcome logs must not be counted as duplicate failures.

