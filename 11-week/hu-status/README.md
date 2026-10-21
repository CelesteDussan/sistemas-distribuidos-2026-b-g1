<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       11-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 11

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Maria Celeste Dussan Ospina
- GITHUB_USER: CelesteDussan
- TEAM: G1
- SPRINT_GOAL: Make the HU-005 absence and HU-002 notification journey observable through trace propagation, structured logs and bounded metrics.
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

## 3. Blockers and risks
- This workshop probe does not prove real broker delivery, PostgreSQL persistence, outbox recovery, Notifications processing or visible guardian notification. HU-005/HU-002 remain doing.
- Local Git DNS failed; separate checkpoint publications used the GitHub connector with verified account metadata. Published history was not rewritten.
- CP4 and closing evidence were published after the October20 20:30 deadline. No on-time completion or Moodle submission is claimed.
- Production integration still needs durable context in the outbox, publisher confirms, approved migrations in the domain DB repository and a real integration environment.

## 4. Plan for next week
- Extend these contracts into the production Attendance adapter and Notifications consumer through the required reviewed HU workflow.
- Verify real broker redelivery, ordering/idempotency and notification persistence; add evidence before changing story completion status.
- Follow up with the separate W11 delivery2: SLOs, alerts, runbook and incident exercise; those files are not part of this delivery.

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
