# Golden signals and metric catalog

## Scope and implementation status

The [absence journey](01-journey.md) crosses `api-gateway`, `attendance-service`, RabbitMQ
and `notifications-service` to process `StudentAbsent` after POST /api/v1/attendance.
[CP3](03-instrumentation/HOW-TO-RUN.md) implements the three Attendance metric families below
in the isolated Java workshop module. Gateway and Notifications metrics are proposed contracts,
not claims about deployed exporters. A queue-depth exporter and real consumer integration remain pending.
Use each environment's own Prometheus; pooled monitoring must additionally filter environment.
Rates below aggregate replicas within that environment. Scrape every 15 seconds; 5m windows
assume sufficient samples. An absent series means missing telemetry, not zero failures.

## api-gateway (proposed)

| Signal | Metric and type | Labels | Dashboard PromQL |
|---|---|---|---|
| Latency | gateway_request_duration_seconds, histogram | route, le on buckets | `histogram_quantile(0.95, sum by (le) (rate(gateway_request_duration_seconds_bucket[5m])))` |
| Traffic | gateway_requests_total, counter | route, outcome | `sum(rate(gateway_requests_total[5m]))` |
| Errors | gateway_requests_total, counter | route, outcome | `sum(rate(gateway_requests_total{outcome="error"}[5m])) / clamp_min(sum(rate(gateway_requests_total[5m])), 0.000001)` |
| Saturation | gateway_active_connections and gateway_connection_limit, gauges | none | `sum(gateway_active_connections) / clamp_min(sum(gateway_connection_limit), 1)` |

Gateway outcomes are success (2xx/3xx), rejected (4xx) and error (5xx), including upstream
connection failures. Measure duration through response completion. The connection-limit exporter
must report effective configured capacity; a missing capacity series cannot justify a healthy ratio.

## attendance-service (implemented in the workshop module)

| Signal | Metric and type | Labels | Dashboard PromQL |
|---|---|---|---|
| Latency | attendance_request_duration_seconds, histogram | route, le on buckets | `histogram_quantile(0.95, sum by (le) (rate(attendance_request_duration_seconds_bucket[5m])))` |
| Traffic | attendance_requests_total, counter | route, outcome | `sum(rate(attendance_requests_total[5m]))` |
| Errors | attendance_requests_total, counter | route, outcome | `sum(rate(attendance_requests_total{outcome="error"}[5m])) / clamp_min(sum(rate(attendance_requests_total[5m])), 0.000001)` |
| Saturation | attendance_requests_in_flight, gauge | none | `sum(attendance_requests_in_flight)` |

Only the normalized Attendance route is counted. An unexpected exception or status>=500 is error;
4xx is rejected, other completed responses success. The separate rejection rate is
`sum(rate(attendance_requests_total{outcome="rejected"}[5m]))`.
Duration uses monotonic elapsed time converted to seconds. In-flight is concurrent requests,
not a utilization percentage; compare it with separately verified thread/connection capacity.
GET /metrics is excluded from the filter and does not inflate business traffic.
The unit test confirms success; real HTTP probes confirmed one503 error and one400 rejection.

## notifications-service (proposed)

| Signal | Metric and type | Labels | Dashboard PromQL |
|---|---|---|---|
| Latency | notification_processing_duration_seconds, histogram | event_type, le on buckets | `histogram_quantile(0.95, sum by (le) (rate(notification_processing_duration_seconds_bucket{event_type="StudentAbsent"}[5m])))` |
| Traffic | notification_events_total, counter | event_type, outcome | `sum(rate(notification_events_total{event_type="StudentAbsent"}[5m]))` |
| Errors | notification_events_total, counter | event_type, outcome | `sum(rate(notification_events_total{event_type="StudentAbsent",outcome="error"}[5m])) / clamp_min(sum(rate(notification_events_total{event_type="StudentAbsent"}[5m])), 0.000001)` |
| Saturation | notification_handlers_in_flight, gauge | event_type | `sum(notification_handlers_in_flight{event_type="StudentAbsent"})` |

One event handling attempt increments exactly one outcome: success, rejected (duplicate/stale
sequence) or error (technical failure). Repeated deliveries are separate attempts, not new logical
notifications. Count unique delivered notifications separately before deriving a user-level SLI.
Processing latency starts on receipt and ends after persistence/acknowledgement handling;
it excludes broker queue wait. In-flight handlers must be compared with configured consumer concurrency.
RabbitMQ ready/unacknowledged depth needs an infra exporter and the actual approved binding names;
this document does not invent queue names or claim its instrumentation exists.

## Bounded labels and estimated series

| Label | Allowed values in this journey | Cardinality estimate | Applies to |
|---|---|---|---|
| route | /api/v1/attendance | 1 | Gateway and Attendance counter/histogram |
| outcome | success, rejected, error | 3 | Request and event counters |
| event_type | StudentAbsent | 1 | Notifications counter/histogram/gauge |
| le | Six finite bucket bounds and +Inf | 7 per histogram | Histogram bucket samples only |

Histogram count/sum have the route or event_type label but no le. Gauges without labels
have one series per target. The estimates below assume one replica and one environment:

- Gateway: counter 1x3=3; histogram 1x(7+2)=9; two gauges=2: total14.
- Attendance: counter 1x3=3; histogram 1x(7+2)=9; one gauge=1: total13.
- Notifications: counter 1x3=3; histogram 1x(7+2)=9; one gauge=1: total13.
- Whole journey: 40 series per environment, excluding standard JVM/process/exporter metrics.
  With two replicas of each service:80. With three such environments:240 stored series.

Prometheus target labels job and instance add target identity; instance count is the replica factor
above. If an environment label is added, its three values are the environment factor, not another multiplier.
Adding GradeCreated later makes event_type cardinality2 and requires recalculating the Notifications total.
Never use student_id, user_id, attendance_id, event_id, trace_id, email or free-text reason as labels.
Use route templates instead of raw URL/query strings, bounded outcome instead of exception messages,
and event_type instead of event_id. High-cardinality identifiers belong only in access-controlled logs.
No exemplars or user identifiers are exposed by this module.

## Latency histogram buckets

| Component | Finite bounds in seconds | User expectation and justification |
|---|---|---|
| Gateway | 0.05, 0.1, 0.25, 0.5, 1, 3 | Proposed: teacher acknowledgement normally within1s; subsecond bins show overhead,3s separates slow dependency calls |
| Attendance | 0.05, 0.1, 0.25, 0.5, 1, 3 | Implemented: align with gateway to compare stages and identify1-3s degradation |
| Notifications | 0.1, 0.5, 1, 3, 5, 10 | Proposed: processing normally within1s, with larger bins to show slow retries/dependencies |

All histograms include +Inf, _sum and _count; finite buckets are cumulative, not independent bins.
The 1s teacher acknowledgement and processing expectations are workshop assumptions, not measured SLOs.
For guardians, total notification delay also includes outbox and broker wait; processing p95 alone
cannot prove timely delivery. A later end-to-end delay metric must start at occurredAt and use
synchronized UTC clocks. Separate monotonic processing timing avoids wall-clock jumps within a process.
When no traffic exists, quantile results may be NaN; display no-data rather than calling it zero latency.
Do not derive a precise percentile above3s from the Attendance histogram: +Inf does not locate that value.

## Consistency checks

- Counter names end in _total; duration histograms use seconds and gauges use counts or capacity.
- Rate is applied before aggregation so counter resets on individual replicas are handled.
- Error panels are technical failure ratios; business rejections have a separate panel.
- Percentile queries aggregate histogram buckets by le, not averages of replica percentiles.
- CP3 tests check cumulative bucket membership and verify /metrics does not count its own scrapes.
- Technical names and domain events match the project docs; proposed metrics require later instrumentation.
