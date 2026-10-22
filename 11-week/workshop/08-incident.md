# Analytic incident drill and blameless postmortem

## Scenario and declared assumptions

This is a synthetic analytic drill, not an executed production incident or observed school traffic.
Use [CP1](01-journey.md)'s asynchronous Attendance outbox -> RabbitMQ hop:25%of its publication
attempts answer in3seconds for40minutes. All eventually succeed; no timeout or5xx is stipulated.
The [CP6](05-slo.md) school forecast is250dailyrecords and25absences. Model200registrations during
this40minute roll-call burst (5/minute),10%absent =>20StudentAbsent publications;5of20 are slow.
Each absence is for a different synthetic student. Baseline broker publication takes100ms; the relay
starts50ms after command receipt, consumer processing100ms. Registration commits/returns in20ms.
Other requests are modeled fast; all20notifications ultimately persist once, with no duplicate effects.
These success/ordering assumptions are inputs to the model, not verified integration behavior.

## One slow trace

trace_id=4bf92f3577b34da6a3ce929d0e0e4736. IDs and times are synthetic.
The trace covers3150ms, while its root HTTP span lasts25ms; asynchronous children outlive HTTP.

- api-gateway POST /api/v1/attendance: span1111111111111111,25ms
  - attendance-service registration: span2222222222222222,20ms
    - AttendanceRecord + outbox transaction: span3333333333333333,18ms
    - StudentAbsent producer: span4444444444444444,3000ms,starts at+50ms after restoring durable context
      - notifications-service process: span5555555555555555,100ms,starts at+3050ms across AMQP boundary
        - Notification persistence: span6666666666666666,80ms included in the100ms consumer span

The source transaction and its HTTP response do not wait for the outbox relay. A slow producer span
therefore delays the guardian alert without automatically making HTTP slow or unsuccessful.

## Four chronological logs in the CP2 contract

```jsonl
{"level":"INFO","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15,"timestamp":"2026-10-21T13:00:00.018Z","service":"attendance-service","span_id":"3333333333333333","event":"attendance.outbox.committed","message":"Absence and publication intent committed","duration_ms":18}
{"level":"INFO","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15,"timestamp":"2026-10-21T13:00:00.020Z","service":"attendance-service","span_id":"2222222222222222","event":"attendance.http.completed","message":"Attendance registration completed before async publication","duration_ms":20}
{"level":"INFO","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15,"timestamp":"2026-10-21T13:00:03.050Z","service":"attendance-service","span_id":"4444444444444444","event":"attendance.publish.completed","message":"StudentAbsent publication completed after slow dependency response","duration_ms":3000}
{"level":"INFO","version":"1.0.0-workshop","environment":"dev","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","correlation_id":"bea6076e-cb45-49fa-9df9-b876b7fe01d9","attendance_id":"e079024f-2fdc-43ed-a96b-5105b3bf3ebf","event_id":"70874170-49f3-4126-93af-19d1f967455b","session_id":"df80a559-1b49-47c4-9877-78d85ec6e3ea","attendance_status":"ABSENT","sequence_number":15,"timestamp":"2026-10-21T13:00:03.150Z","service":"notifications-service","span_id":"5555555555555555","event":"notification.event.processed","message":"StudentAbsent processed and notification committed","duration_ms":100}
```

## SLI results, alerts and budget impact

| Indicator | Modeled result during40minutes | Incremental budget consumed |
|---|---|---|
| Attendance availability | 200successful/200valid=100% | 0failed registrations of50monthly allowance |
| Attendance<=0.5s | 200fast/200completed=100% | 0slow HTTPrequests of250allowance |
| Eligible notification-processing reliability | 20successful/20valid=100% | 0technical failed attempts of5allowance |
| Guardian end-to-end delay (not an existing CP6 SLI) | 5of20alerts take3150ms vs250ms baseline | No defined delay budget; this is the coverage gap |

Neither AttendanceFastBudgetBurn nor AttendanceSlowBudgetBurn fires at any minute:technical-error
ratio is0in every nonempty window, below14.4%and6%. Assume baseline samples exist so this is not
missing-data suppression. Slowness by itself cannot manufacture a failing availability request.
Availability-budget minutes consumed=0; cumulative extra delay is5*(3-0.1)=14.5seconds and is NOT downtime.
One affected publication spans3s; five guardianalerts are delayed, without data loss in this model.
In the different synchronous CP3probe,25%of20AMQP-backed HTTPcalls at3s would give latency SLI75%
(15/20fast), but availability remains100%. That probe must not replace CP1's asynchronous production trace.

For comparison ONLY, a separate hypothetical25%5xx scenario with stationary traffic would cross
fast1h threshold after(0.144/0.25)*60=34.56minutes, next30s evaluation35minutes; its short5m window
already exceeds threshold. At40minutes the6h ratio0.25*40/360=2.78%is below6%, so slow would not fire.
These conditional calculations do not describe the supplied slow-but-successful incident.

## Response and timeline (modeled)

| Relative time | Observation or response |
|---|---|
| BeforeT0 | Baseline broker100ms; trace context retained in outbox; no technical HTTPerrors |
| T0 | Inject analytic3s latency into25%of producer calls;40minute affected interval begins |
| T+5m | A manual guardian-delay check and trace review identify long producer spans; availability pages remain silent |
| T+10m | Coordinator applies runbookstep1 in the controlled drill:stop additional synthetic repeated load, preserve normal registration and committed events |
| T+10m toT+40m | Existing affected calls still incur modeled dependency delay; monitor pending work and do not pretend load reduction repaired the broker |
| T+40m | Injected delay ends; broker baseline returns, affected work has succeeded, owners check one effect per event |
| AfterT+40m | Integrated recovery checks and replay/ordering tests remain to be executed in a real environment |

The runbook's step1 stops additional test-induced accumulation, not the existing dependency's response
time. The injected40minute fault expires on schedule; no rollback or broker restart was executed.
For a real bottleneck, the infra owner must confirm cause before attempting runbookstep3 recovery.

## Blameless analysis

**Impact:**200modeledregistrations,20absenceevents,5delayedguardianalerts;0lostrecords or failed attempts
under the stated assumptions. Delay may reduce guardian trust even while all currently defined SLIs pass.
**Root cause in this drill:**deliberate3s response delay on25%of brokerpublication calls. There is no
production root-cause proof. Broker resource contention/network delay would be hypotheses requiring telemetry.
**Contributing factors:**availability-only pages; processing latency excludes producer/queue wait;
no deployed end-to-end age metric; durable outbox trace restoration still needs integration evidence.
No person is blamed and no real operator action or outage is invented.

## Proposed follow-up stories with testable acceptance

| Proposal | User story | Acceptance criteria |
|---|---|---|
| OBS-01 | As a guardian, I need delayed absence alerts to be detected even when HTTP succeeds | Add occurredAt-to-persisted-notification duration with bounded labels and a2s boundary; replay20events with5delayed and verify75%<=2s; a reviewed delay alert activates while availability alert stays inactive |
| OBS-02 | As the Attendance owner, I need trace context to survive outbox delay and retry | Persist publication context through an approved domain-DB migration; restart the relay and verify the same trace_id/eventId with fresh attempt spans; publish no migration inside the APIrepository |
| OBS-03 | As the Notifications owner, I need slow/repeated deliveries to produce one ordered effect | Real RabbitMQ/PostgreSQL tests cover duplicateeventId,delayedsequence,gap and recovery; verify one persisted notification and no overwrite by an older event; link runtime evidence before marking the HUdone |

These are proposals, not created team cards or completed implementation. Availability and latency
coverage must be reviewed together before choosing which follow-up to implement first.

## Verification scope

Local deterministic checks validate counts, budgets, conditional alert math, all four JSONobjects and
CP2fields/trace identity. No live fault injection, production postmortem, Prometheus evaluation or
measured end-to-end delivery is claimed. [Runbook](07-runbook.md) supplies the proposed recovery checks.
