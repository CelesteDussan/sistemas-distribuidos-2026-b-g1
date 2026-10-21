# Run the Attendance observability module

Java 21 and Maven 3.9+ are required. From `03-instrumentation/educk-attendance-api`:
```powershell
mvn package -DskipTests
java -jar target/educk-attendance-api-workshop-1.0.0-workshop.jar
```

In a second terminal:
```powershell
curl.exe -i -X POST http://127.0.0.1:18083/api/v1/attendance -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" -H "X-Correlation-Id: bea6076e-cb45-49fa-9df9-b876b7fe01d9"
```

The first CP3 publication instruments HTTP only. Status 202 means the probe accepted
an instrumentation request; it does not claim an AttendanceRecord was persisted.
This isolated module does not modify the real Attendance application, connect to a database,
or deliver a StudentAbsent notification. AMQP propagation, metrics and the automated test follow in CP3 part 2.

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
