# Run the Attendance observability module

Java 21 and Maven 3.9+ are required. From `03-instrumentation/educk-attendance-api`:
```powershell
mvn verify
java -jar target/educk-attendance-api-workshop-1.0.0-workshop.jar
```

In a second terminal:
```powershell
curl.exe -i -X POST http://127.0.0.1:18083/api/v1/attendance -H "Content-Type: application/json" -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" --data-binary "@sample-absence.json"
curl.exe http://127.0.0.1:18083/metrics
```

The first CP3 publication instrumented HTTP only; its historical output is below.
The final module publishes a synthetic StudentAbsent envelope to RabbitMQ and returns 202 after
the client send completes; this is not a publisher confirm or proof of notification delivery.
Use an isolated dev broker; configure SPRING_RABBITMQ_HOST and SPRING_RABBITMQ_PORT for that broker.
If unavailable, the probe returns503 and logs the technical failure; invalid absence input returns400.
There is no database, authorization, durable outbox, automatic retry or Notifications consumer here.
This module is an adapter experiment, not a replacement for the real Attendance registration service.
Production integration must reuse persisted IDs/sequences, restore durable context and confirm delivery.
Never deploy this unauthenticated probe; it binds to loopback and uses synthetic fixtures only.

## Actual local execution, 2026-10-20 (America/Bogota)

Built using Java 21 and Maven 3.9.6, offline with the existing dependency cache:

```text
Compiling 5 source files with javac [debug parameters release 21]
Tests are skipped.
BUILD SUCCESS
Total time: 7.550 s
Finished at: 2026-10-20T19:34:01-05:00
```

The initial server start failed with `java.net.SocketException: Permission denied: connect`.
The execution sandbox blocked Tomcat's loopback selector; rerunning with local-network permission started it without a code change.
Three real HTTP probes returned 202: valid trace preserved with fresh span; missing and all-zero contexts replaced.
Actual valid response: `traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-eb6f3c9d2130ec3e-01`.
Actual application log:

```json
{"timestamp":"2026-10-21T00:34:55.272248700Z","level":"INFO","service":"attendance-service","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"eb6f3c9d2130ec3e","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","event":"attendance.http.completed","message":"Attendance HTTP operation completed","duration_ms":64.1352}
```

## CP3 part 2: automated checks and actual output

`mvn verify` resolves dependencies, runs tests and packages the application.
For this machine the offline run used Java 21, Maven 3.9.6 and the existing local Maven cache.

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 8.362 s
Finished at: 2026-10-20T19:52:00-05:00
```

The four automated tests cover incoming-to-AMQP trace identity and distinct child spans,
correlation propagation and StudentAbsent envelope, invalid contexts, cumulative buckets,
request outcomes, GET /metrics, broker failure503 and business rejection400 without publishing.
RabbitTemplate is mocked: header propagation is proved at the adapter boundary, not across a live broker.

The actual HTTP runtime used `--spring.rabbitmq.port=1 --spring.rabbitmq.connection-timeout=300ms`
to make the dependency deliberately unavailable, without contacting any real project broker:

```text
POST /api/v1/attendance (valid synthetic absence) -> 503 {"error":"broker_unavailable"}
POST /api/v1/attendance (invalid status) -> 400 {"error":"invalid_absence"}
GET /metrics -> 200
attendance_requests_total{route="/api/v1/attendance",outcome="rejected"} 1
attendance_requests_total{route="/api/v1/attendance",outcome="error"} 1
attendance_request_duration_seconds_bucket{route="/api/v1/attendance",le="+Inf"} 2
attendance_request_duration_seconds_count{route="/api/v1/attendance"} 2
attendance_request_duration_seconds_sum{route="/api/v1/attendance"} 0.2183893
attendance_requests_in_flight 0
```

Finite buckets are 0.05, 0.1, 0.25, 0.5, 1 and 3 seconds, plus +Inf.
Scraping /metrics creates no business request and is excluded from the trace filter.

## Problems encountered and resolution

- Tomcat startup: sandbox loopback connection denied; approved local-network execution worked without a source change.
- Publisher compilation: `RabbitTemplate.send` selected a CorrelationData overload; changed to `convertAndSend` with MessagePostProcessor to inject AMQP headers.
- Test execution: sandbox denied the Java-agent named pipe; stopped the stalled run and executed Maven with approved local permissions. Final run passed all four tests.
- Metrics test: standalone MVC initially had only a JSON converter, causing500 on its text response. Added StringHttpMessageConverter and asserted HTTP200 plus the metrics body. No application-route change was needed.

Remaining acceptance: real broker delivery/confirmation, durable outbox recovery, database persistence,
consumer processing and a notification visible in the portal have not been validated by this workshop.
