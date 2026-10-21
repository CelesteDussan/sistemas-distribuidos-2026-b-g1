# Critical journey: record an absence and notify the guardian

## Scope and sources

HU-005 records an absent student; HU-002 delivers one guardian notification.
The journey follows `api-gateway`, `attendance-service`, RabbitMQ and
`notifications-service`, using `StudentAbsent` and an Attendance outbox.

- [Product vision, KR2](https://github.com/code-corhuila/educk-docs/blob/main/03-product/vision.md)
- [Architecture, publish/subscribe flow](https://github.com/code-corhuila/educk-docs/blob/main/05-architecture/overview.md)
- [Attendance endpoint](https://github.com/code-corhuila/educk-docs/blob/main/09-microservices/services/05-attendance/README.md#endpoints-clave)
- [Canonical StudentAbsent envelope](https://github.com/code-corhuila/educk-docs/blob/main/02-domain/domain-events.md#event-studentabsent)
- [Delivery and ownership](https://github.com/code-corhuila/educk-docs/blob/main/09-microservices/event-catalog.md)

The source architecture contains older database-instance diagrams. This workshop follows
Annex J: one PostgreSQL instance per environment, domain schemas and isolated migrations.
No physical cross-domain foreign keys or writes are introduced.
The local repository is `educk-attendance-api`; its logical service name is `attendance-service`.
This is an instrumentation proposal. The diagram does not certify a deployed integration.

## Hop map

| Step | Caller | Callee | Protocol / operation | Mode | Owned data | Trace context |
|---|---|---|---|---|---|---|
| 1 | Teacher portal | api-gateway | HTTP POST /api/v1/attendance | Sync | None | Incoming traceparent or new root |
| 2 | api-gateway | attendance-service | HTTP POST /api/v1/attendance | Sync | AttendanceRecord | Forward traceparent; create server child span |
| 3 | attendance-service | Attendance schema | SQL: record ABSENT and outbox atomically | Sync | Attendance record and outbox | Local DB child span; persist publication context |
| 4 | Attendance outbox relay | RabbitMQ | AMQP StudentAbsent, educk.attendance.student.absent | Async | Outbox delivery status | New producer span; traceparent and correlation header |
| 5 | RabbitMQ delivery | notifications-service | AMQP consume StudentAbsent | Async | Processed-event state | Extract headers; create consumer child span |
| 6 | notifications-service | Notifications schema | SQL: deduplicate eventId and store alert | Sync | Notification | Local DB child span |

## Sequence diagram

```mermaid
sequenceDiagram
    participant UI as Teacher portal
    participant GW as api-gateway
    participant ATT as attendance-service
    participant ADB as Attendance schema
    participant RELAY as Attendance outbox relay
    participant MQ as RabbitMQ
    participant NOT as notifications-service
    participant NDB as Notifications schema
    UI->>GW: POST /api/v1/attendance (traceparent or new root)
    GW->>ATT: POST /api/v1/attendance (traceparent child context)
    ATT->>ADB: Save ABSENT + outbox (local span; persist traceparent)
    ADB-->>ATT: Transaction committed (same trace_id, DB span)
    ATT-->>GW: 201 Created (response traceparent)
    GW-->>UI: 201 Created (response traceparent)
    RELAY->>ADB: Read pending outbox (restore saved traceparent)
    RELAY-)MQ: StudentAbsent (AMQP traceparent + correlation_id)
    MQ-)NOT: Deliver event (AMQP traceparent + correlation_id)
    NOT->>NDB: Deduplicate eventId and save alert (local child span)
    NDB-->>NOT: Commit notification (same trace_id)
    NOT-->>MQ: ACK after commit (delivery context; no new trace)
```

## Span tree

All spans below share trace_id `4bf92f3577b34da6a3ce929d0e0e4736`.

- api-gateway POST /api/v1/attendance: root if the portal supplies no valid context
  - attendance-service POST /api/v1/attendance: child server span
    - Attendance transaction: write AttendanceRecord and outbox
    - StudentAbsent publish: producer span restored later from durable outbox context
      - notifications-service StudentAbsent process: consumer span across async boundary
        - Notifications transaction: eventId deduplication and Notification write

Persist the producer parent context in the outbox alongside the event; restoring only
correlation_id cannot reconstruct the trace. The HTTP span may finish before its
asynchronous descendants. Each hop gets a fresh span_id; trace_id stays unchanged.
A publication retry preserves eventId and correlation_id, with a fresh attempt span.
A delayed consumer extracts the AMQP traceparent rather than starting an unrelated root.

## Instrumentation boundary and failure to observe

CP3 will instrument the Attendance HTTP boundary and its outbound AMQP publication adapter.
The automated test will capture the AMQP message headers; the broker can be mocked
for the unit test, which is distinct from a real RabbitMQ integration test.
A broker timeout must leave the committed absence and publication intent available
for retry. A future-date rejection occurs before persistence and emits no StudentAbsent.
The production outbox relay still needs durable trace-context storage and publisher confirms;
this workshop will document those integration gaps instead of claiming them complete.
