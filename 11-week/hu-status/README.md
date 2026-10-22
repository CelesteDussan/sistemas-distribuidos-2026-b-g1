<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Maria Celeste Dussan Ospina
- GITHUB_USER: CelesteDussan
- TEAM: G1
- SPRINT_GOAL: Define and verify observability and reliability contracts for the HU-005 absence and HU-002 guardian notification journey.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-005 | Observe absence registration and publication | doing | [Instrumentation and test evidence](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/cef9b297400ce8abac0d09d91bfd7711819a4d73) |
| HU-002 | Trace StudentAbsent towards guardian notification | doing | [Journey map](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/99c2ea8f96794401762ea572cfa9d48ffc840d7d) |

## 2. My individual contribution
- Documented the Gateway, Attendance, RabbitMQ and Notifications journey, including asynchronous spans and outbox context requirements.
- Defined twelve common JSON log fields, five domain fields, privacy rules and seven synthetic logs covering publisher timeout and stale-sequence rejection.
- Built an isolated Java21/Spring Boot module with validated trace context, structured logging, AMQP header propagation and a bounded Prometheus exporter.
- Ran four automated tests: trace/correlation propagation, invalid contexts, cumulative buckets/outcomes, broker failure and business rejection. All passed; no tests skipped.
- Verified real HTTP503 for a deliberately unavailable broker,400 for invalid input and200 on /metrics, with correct counters and histogram count.
- Defined four golden signals for three services, PromQL, label cardinality estimates and latency buckets; distinguished implemented Attendance metrics from proposed metrics.
- [Reproduction commands and actual console output](../workshop/03-instrumentation/HOW-TO-RUN.md).

### Delivery 2 technical contribution
- Specified three rolling30-day SLIs/SLOs using CP4metrics and the official99%MVP availability/500msAttendance targets.
- Documented a hypothetical250-student school:5,000monthlyregistrations,500absenceevents; budgets50HTTPerrors,250slowrequests and5technicalevent-processingfailures. These are estimates, not production counts.
- Added multiwindow availability alerts14.4x(1h/5m) and6x(6h/30m), plus a traffic-sensitive budget policy. CachedSnakeYAML2.2 parsed the rules; no promtool/livePrometheus evaluation is claimed.
- Wrote a runbook with trace/PromQL diagnosis, three ordered mitigations, role escalation and data-preserving recovery checks. Recovery operations were not executed.
- Modeled the3s/25%/40min asyncbroker incident, four validatedJSONlogs and a blameless postmortem with three proposed stories. A slow successful broker does not cause HTTPerrors in an asyncoutbox journey, so neither availability alert fires.
- Checked modelcounts200registrations/20events/5slow,14.5extra-delayseconds,zeroavailabilitybudgetconsumption and the separately labeled hypothetical5xxalert math.
- Published CP6/CP7 in the first work block, then began CP8 after a42-minute break. CP8/CP9/CP10 form the second block; checkpoint publications are separated by at least15minutes.
- Student-owned reflection entriesCP6-CP9 and the minimumone-page implementation plan were not written by this technical workflow and are not verified complete.

## 3. Blockers and risks
- This workshop probe does not prove real broker delivery, PostgreSQL persistence, outbox recovery, Notifications processing or visible guardian notification. HU-005/HU-002 remain doing.
- Local Git DNS failed; separate checkpoint publications used the GitHub connector with verified account metadata. Published history was not rewritten.
- CP4 and closing evidence were published after the October20 20:30 deadline. No on-time completion or Moodle submission is claimed.
- Production integration still needs durable context in the outbox, publisher confirms, approved migrations in the domain DB repository and a real integration environment.

- Moodle assignment text specifies D2deadline Wednesday2026-10-21 23:59-05:00, overriding Thursday10:10 in the PDF. CP7andlater were published after that deadline; no extension or submission is verified.

## 4. Plan for next week
- Extend these contracts into the production Attendance adapter and Notifications consumer through the required reviewed HU workflow.
- Verify real broker redelivery, ordering/idempotency and notification persistence; add evidence before changing story completion status.
- Turn the Delivery2 proposals into reviewed implementation stories; validate the missing end-to-end delay metric and real broker/consumer behavior before claiming reliability compliance.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

This individual workshop lives in a personal fork branch; it is not a production HU promotion PR.
Each checkpoint was published separately, with at least fifteen minutes between publications.
CP3 required two publications; its second publication contains two commits of99 and131 additions.
The integration checkbox above is intentionally unchecked. Existing production domain code was not changed.

## 6. Evidence links
- [CP0: setup](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/9a5a623cf65f4ec71376d33fd003ec75dcb09505)
- [CP1: journey](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/99c2ea8f96794401762ea572cfa9d48ffc840d7d)
- [CP2: log contract](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/a07c92ac16c728ff42a39e824153e726d4ed090a)
- [CP3 first publication: middleware/logger](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/8a2e6fefb41f6988dc094bb2ba5be7337b6c6a4a)
- [CP3 second publication: AMQP/metrics](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/34dd91c5d88f5659e10d2837c776a30a2172c290)
- [CP3 second publication: tests and execution evidence](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/cef9b297400ce8abac0d09d91bfd7711819a4d73)
- [CP4: metric catalog](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/419a6e82e0401c0db0316931da894bd77b8e4416)
- [Workshop directory](../workshop/README.md)
- [Personal reflection document](https://docs.google.com/document/d/112slj18F1BwunuOD7-e-P3Djnzv7Bv57qoX09uXK10I/edit)

### Delivery 2 evidence
- [CP6: SLOs and budgets](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/633378da208c93934b36fa138d26ca86682d15e6)
- [CP7: burn-rate rules and policy](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/a55459beda91ff06ef93a5d2f9c81d32553a188b)
- [CP8: runbook](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/ae773bfc250cb2978822e7b7df1b79291437dbb6)
- [CP9: analytic incident and postmortem](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/9df2794220c6ffd8023a5f99518f780fe48ba33d)
- CP10 closes this weekly report and the workshopindex; its resulting commit is available in the [branchhistory](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commits/codex/w11-observability).
- [SLO artifact](../workshop/05-slo.md), [alert rules](../workshop/06-alerts.yml), [policy](../workshop/06-alert-policy.md), [runbook](../workshop/07-runbook.md), [incident](../workshop/08-incident.md).
