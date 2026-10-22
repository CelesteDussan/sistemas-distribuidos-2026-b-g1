# Runbook: AttendanceFastBudgetBurn

## Scope and trigger

Owner: G1 Attendance domain owner; incident coordinator assigns supporting roles below.
Journey: Gateway -> attendance-service -> outbox -> RabbitMQ -> notifications-service, StudentAbsent.
Use [CP6 SLOs](05-slo.md), [CP7 rules](06-alerts.yml) and [log contract](02-log-contract.md).
Trigger: technical-error ratio exceeds14.4% in BOTH1h and5m windows for the99%availability objective.
The rule evaluates every30s without an additional for-delay. A latency-only incident does not trigger it.
This is a proposed operating procedure, not a record of recovery actions already executed.

## Symptoms and first checks

- Teacher registration attempts return5xx or dependency errors; guardian absence alerts may be delayed.
- Separate4xx validation/RBAC rejections from technical failures; do not retry malformed commands blindly.
- Confirm the affected environment, route, service version, deployment time and target health first.
- Record incident start/timezone, assigned coordinator and one synthetic or access-controlled trace_id.
- If the page is absent but guardians report delay, enter latency/backlog diagnosis manually; do not wait for a5xx page.

## Confirm with metrics

Run the two ratios against the SAME environment's Prometheus and compare both with0.144:

```promql
sum(rate(attendance_requests_total{route="/api/v1/attendance",outcome="error"}[1h])) / sum(rate(attendance_requests_total{route="/api/v1/attendance",outcome=~"success|error"}[1h]))
sum(rate(attendance_requests_total{route="/api/v1/attendance",outcome="error"}[5m])) / sum(rate(attendance_requests_total{route="/api/v1/attendance",outcome=~"success|error"}[5m]))
```

Compare traffic, tail latency, concurrent requests and raw failure counts:

```promql
sum(rate(attendance_requests_total{route="/api/v1/attendance"}[5m]))
histogram_quantile(0.95, sum by (le) (rate(attendance_request_duration_seconds_bucket{route="/api/v1/attendance"}[5m])))
sum(attendance_requests_in_flight)
sum(increase(attendance_requests_total{route="/api/v1/attendance",outcome="error"}[1h]))
```

No samples or zero valid attempts means unknown/no traffic, not proof of recovery.
CP4 notification-processing metrics exclude outbox/broker wait: inspect approved queue/outbox backlog
through the owning infrastructure/domain tools; do not infer end-to-end delivery from the HTTP p95.

## Follow one trace in structured logs

For a locally collected JSON-only log file, substitute the incident's actual trace identifier:

```powershell
Get-Content ./attendance.jsonl | ForEach-Object { $_ | ConvertFrom-Json } | Where-Object { $_.trace_id -eq '4bf92f3577b34da6a3ce929d0e0e4736' } | Sort-Object timestamp | Select-Object timestamp,service,span_id,correlation_id,event,level,duration_ms
```

The identifier above is the synthetic CP1 example, not a production student's trace.
Search authorized logs from Gateway, Attendance and Notifications with the same trace_id;
compare span_id,parent relationships,timestamps and event_id for redelivery. Keep raw payloads,
student names, tokens and credentials out of incident notes. Retain outbox publication context.
If the async consumer starts a different trace, record the propagation gap instead of claiming a complete trace.

## Mitigations, in increasing risk

| Order | Action and owner | Preconditions and data protection | Verification |
|---|---|---|---|
| 1 | Coordinator stops controlled test/load generation or asks operators to stop repeated manual resubmissions | Only excess/repeated traffic is paused; normal registration and committed records remain available; no service shutdown or payload deletion | Compare traffic, latency and in-flight work; verify existing pending events can still progress |
| 2 | Deployment owner rolls back the affected application image to the last reviewed known-good artifact if a recent rollout correlates with the failure | Confirm compatibility and existing deployment procedure; obtain required reviews; do not revert migrations, rewrite environment branches or self-merge | Confirm restored version/health and authorized synthetic registration, then compare both error windows |
| 3 | Infra owner restores confirmed RabbitMQ connectivity/health through the existing environment deployment procedure | Confirm broker is the cause before restart/reconfiguration; preserve volumes, messages, eventId and ordering keys; no mass replay/scaling before idempotency is verified | Broker health and connectivity recover; controlled replay creates one effect per event and drains the confirmed backlog |

No feature flag or circuit-breaker switch is assumed to exist. A long broker response with no errors
may only require step1 during a controlled drill plus dependency diagnosis; a rollback or restart is not automatic.
For production traffic, stopping the test generator is not a substitute for diagnosing a real dependency bottleneck.

## Infrastructure diagnosis and escalation

The inspected infrastructure Compose defines the rabbitmq service and rabbitmq-diagnostics health check.
The infra owner first selects the approved environment/project/local configuration, then may inspect:

```text
docker compose ps
docker compose exec rabbitmq rabbitmq-diagnostics -q ping
```

These are read-only diagnostic examples, not commands executed by this workshop. Never select another
environment's deployment by accident. Broker recovery must preserve its persistent volume.

| Role | Responsibility | Escalation condition |
|---|---|---|
| Incident coordinator (G1) | Record timeline, assign owners and choose reversible mitigation | Immediately on confirmed fast-burn page or substantial user-visible delay |
| Attendance owner | Check HTTP failures, transaction/outbox boundary and trace propagation | Any source-record failure or durable-publication gap |
| Infra owner | Verify environment, broker health/connectivity and deployment state | Broker unavailable, persistent dependency latency or growing confirmed backlog |
| Notifications owner | Check idempotency, sequence handling and eventual alert persistence | Event accepted by broker but no guardian notification, or duplicate/stale effects |
| Course/team reviewer | Review risky changes and unresolved architecture blockers | Mitigation requires a reviewed change, cross-owner decision or cannot restore service |

No messages are sent by this document. Do not assign an unverified person or bypass CODEOWNERS.

## Verify recovery and close

1. Check metrics are present and BOTH fast-window ratios fall below0.144; also inspect remaining30-day budget.
2. During a sustained normal-traffic observation period (at least one5m window), confirm low error count,
   response latency near the500ms target and stable in-flight work. Do not confuse alert reset with full budget recovery.
3. In the actual integrated environment, perform one authorized synthetic absence registration; verify the
   Attendance record/outbox transaction, event delivery, one persisted notification and guardian query visibility.
4. Verify same trace_id across the async boundary, no duplicate effect after controlled retry, preserved
   ordering and backlog returning toward baseline. Keep evidence links without personal data.
5. Record what changed, residual risk, reviewers and follow-up stories in a blameless incident record.

The CP3 probe's202 and mocked RabbitTemplate test cannot satisfy steps3-4; they lack durable source
persistence and a live consumer. Those recovery checks remain integration work, not completed evidence.
References: [CP1 journey](01-journey.md), [metric catalog](04-metrics.md),
[official infrastructure](https://github.com/code-corhuila/educk-infra) and
[canonical StudentAbsent](https://github.com/code-corhuila/educk-docs/blob/main/02-domain/domain-events.md#event-studentabsent).

