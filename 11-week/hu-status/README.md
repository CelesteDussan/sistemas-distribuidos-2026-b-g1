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
- Defined structured log fields, privacy rules and synthetic examples for publisher timeout and stale-sequence rejection.
- Built an isolated Java 21/Spring Boot observability probe with trace context validation, structured logging, AMQP header propagation and Prometheus metrics.
- Ran four automated tests covering trace propagation, invalid contexts, metric buckets and outcomes, broker failure and business rejection; all passed.
- Verified HTTP 503 for an unavailable broker, HTTP 400 for invalid input and HTTP 200 from `/metrics`.
- Defined service signals, PromQL, cardinality estimates, latency buckets, SLOs, alert policies, a runbook and an analytic incident model. Proposed metrics and hypothetical calculations are labeled as such.

## 3. Blockers and risks
- The isolated probe does not prove live broker delivery, PostgreSQL persistence, outbox recovery, Notifications processing or a visible guardian notification. HU-005 and HU-002 remain `doing`.
- Production integration still requires durable trace context, publisher confirms, approved database migrations and real integration tests.
- Delivery checkpoints CP4/CP5 and CP7 onward were published after the recorded deadlines. Moodle submission is not verified.
- Student-authored reflection entries and the implementation plan are not verified complete.

## 4. Plan for next week
- Integrate observability contracts through the reviewed HU workflow.
- Verify real broker redelivery, ordering/idempotency and notification persistence before changing story status.
- Validate the missing end-to-end delay metric and real consumer behavior.
- Complete student-owned reflection and submission requirements, then verify their status.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

The workshop is individual evidence on a personal fork branch, not a production HU promotion PR. Existing production domain code was not changed; real broker, persistence and end-to-end notification acceptance remain unverified.

## 6. Evidence links
- [Workshop overview](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/blob/codex/w11-observability/11-week/workshop/README.md)
- [Reproduction guide and test output](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/blob/codex/w11-observability/11-week/workshop/03-instrumentation/HOW-TO-RUN.md)
- [CP0: setup](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/9a5a623cf65f4ec71376d33fd003ec75dcb09505)
- [CP1: journey map](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/99c2ea8f96794401762ea572cfa9d48ffc840d7d)
- [CP2: structured log contract](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/a07c92ac16c728ff42a39e824153e726d4ed090a)
- [CP3: implementation and tests](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/cef9b297400ce8abac0d09d91bfd7711819a4d73)
- [CP4: metric catalog](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/419a6e82e0401c0db0316931da894bd77b8e4416)
- [CP6: SLOs and budgets](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/633378da208c93934b36fa138d26ca86682d15e6)
- [CP7: burn-rate rules and policy](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/a55459beda91ff06ef93a5d2f9c81d32553a188b)
- [CP8: runbook](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/ae773bfc250cb2978822e7b7df1b79291437dbb6)
- [CP9: incident model and postmortem](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/9df2794220c6ffd8023a5f99518f780fe48ba33d)
