# SLIs, SLOs and error budgets: absence registration and notification

## Scope, sources and measurement boundary

The journey is POST /api/v1/attendance through `api-gateway`, `attendance-service`, RabbitMQ
and `notifications-service`, which processes `StudentAbsent` for the guardian.
[Project NFR-001/002](https://github.com/code-corhuila/educk-docs/blob/main/04-requirements/non-functional.md)
sets the MVP production availability target at99% and attendance registration p95 below500ms.
These are production acceptance objectives; dev/QA are best effort. No existing deployment
is certified compliant by this document. [CP4](04-metrics.md) defines the metric names used below.
CP3 is an isolated publication probe without a database/outbox: its202 is not evidence of durable attendance storage.
Before production SLO measurement, success must mean a committed Attendance record and outbox intent.
Notification metrics from CP4 remain proposed and need instrumentation before their SLO can be evaluated.

## Three rolling 30-day objectives

| SLO | Good events / valid events (SLI) | Objective | Window |
|---|---|---|---|
| Attendance availability | successful completed registration attempts / successful+technical-error attempts | >=99% | Rolling30d |
| Attendance latency | all completed route requests with duration<=0.5s / all completed route requests | >=95% | Rolling30d |
| StudentAbsent processing reliability | successfully processed eligible event attempts / success+technical-error event attempts | >=99% | Rolling30d |

A technical HTTP failure is outcome=error (5xx or unhandled exception); a4xx business rejection
is not a valid availability attempt. Latency includes4xx because CP3 histogram has no outcome label;
its denominator intentionally differs and must not be described as successful-request latency.
The0.5s bucket operationalizes the500ms boundary; do not infer latency from availability counters.
Notification duplicate/stale rejections are excluded as expected outcomes. Retries count as attempts,
not new logical notifications; this third SLI does not prove one alert per unique event or end-to-end delivery.
It complements, rather than replaces, eventId idempotency tests and broker/outbox backlog monitoring.

### Exact PromQL: Attendance availability

```promql
sum(increase(attendance_requests_total{route="/api/v1/attendance",outcome="success"}[30d]))
/
sum(increase(attendance_requests_total{route="/api/v1/attendance",outcome=~"success|error"}[30d]))
```

### Exact PromQL: Attendance latency

```promql
sum(increase(attendance_request_duration_seconds_bucket{route="/api/v1/attendance",le="0.5"}[30d]))
/
sum(increase(attendance_request_duration_seconds_count{route="/api/v1/attendance"}[30d]))
```

### Exact PromQL: StudentAbsent processing reliability

```promql
sum(increase(notification_events_total{event_type="StudentAbsent",outcome="success"}[30d]))
/
sum(increase(notification_events_total{event_type="StudentAbsent",outcome=~"success|error"}[30d]))
```

Evaluate in one environment's Prometheus; aggregate replicas only within that environment.
Apply increase before sum to handle replica counter resets. Retain >=30d of samples.
A missing metric or zero denominator is unknown/no traffic, not100% availability; investigate telemetry
separately. CP7 uses short-window bad/valid ratios from the same availability definition.

## Explicit small-school planning scenario (not measured traffic)

This hypothetical scenario was selected for the workshop because actual enrollment/traffic was unavailable.
Assume one small school with250 students (ten groups of25), one attendance registration per student
per school day,20 school days in a30-calendar-day window, and six active school hours each day.
That gives250 registration attempts/day and5,000/month, assuming no steady-state retries or invalid input.
Assume10% are absences:25 StudentAbsent events/day and500 processing attempts/month without retries.
These counts are forecasts, not an actual school roster, database count, benchmark or analytics result.
Replace them with measured volume when available. Observed retries/rejections change the denominators.
Average school-hour registration load is250/360=0.6944 requests/minute; a5/minute peak for40minutes
would account for200 of the250 daily registrations and is plausible during a concentrated roll call.
This peak assumption is available for the CP9 analytic drill; it is not a measured incident.

## Budget arithmetic

For target S, allowed bad fraction=1-S. Budget in events=N*(1-S).
Calendar-equivalent minutes=30*24*60*(1-S); active-school equivalent=20*6*60*(1-S).

| Objective | Forecast eligible events | Allowed bad events | Calendar-equivalent minutes | Active-school-equivalent minutes |
|---|---|---|---|---|
| Availability99% | 5,000 | 50 failed attempts | 432 | 72 |
| Latency95% | 5,000 | 250 requests slower than0.5s | 2,160 | 360 |
| Notification processing99% | 500 | 5 failed processing attempts | 432 | 72 |

Minute figures are uniform-traffic equivalents, not an additional time-based uptime allowance.
Actual request loss depends on traffic at the time: at a5/min peak,50 errors consume the entire
availability budget in10minutes of complete failure, rather than432calendar minutes.
A registration error budget does not permit data loss. Outbox retries must preserve eventId,
correlation_id and trace context; a notification failure must not roll back committed Attendance data.
At low volume one failed notification consumes20% of its five-attempt monthly budget.

## Error-budget policy (applied to each objective independently)

| Consumed budget | Team action |
|---|---|
| <50% | Continue reviewed work; watch burn and inspect rejected requests separately |
| >=50% | Domain owner investigates traces/logs, prioritizes reliability fixes and reviews remaining rollout risk |
| >=75% | Pause discretionary feature rollout for the affected journey; require recovery evidence and another G1 reviewer before resuming |
| >=100% | Freeze feature promotion affecting the journey; focus on reviewed recovery/security fixes until the rolling budget recovers |

Record decisions in the relevant HU and incident record. Authors do not approve/merge their own PRs,
and no protected environment branch is force-updated. Coordinate Attendance and Notifications owners
without logging student names, tokens or event payloads containing personal data.

## Validation and limitations

Budget calculations checked for5,000/500 events,30days and20six-hour school days.
Queries use CP4 labels and the actual Attendance0.5s bucket; no metric or bucket was silently invented.
PromQL is a reproducible specification; no live30-day dataset or production SLO result is claimed.
A3s successful downstream response is slow but is not automatically an availability failure.
CP9 must distinguish latency degradation from actual5xx; an unstated timeout cannot manufacture errors.

