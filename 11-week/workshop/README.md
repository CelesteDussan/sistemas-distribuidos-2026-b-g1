# Week 11 - Observability workshop

<!-- CONFIG-START -->
- FULL_NAME: Maria Celeste Dussan Ospina
- GITHUB_USER: CelesteDussan
- TEAM: G1
- DOCS_REPO: https://github.com/code-corhuila/educk-docs
- REFLECTION_DOC: https://docs.google.com/document/d/112slj18F1BwunuOD7-e-P3Djnzv7Bv57qoX09uXK10I/edit
- JOURNEY: Record an absence and notify the guardian (HU-005 / HU-002).
<!-- CONFIG-END -->

## Project context

EduTrack documents Identity, Attendance and Communication in its
[service documentation](https://github.com/code-corhuila/educk-docs/tree/main/09-microservices/services).
The critical journey and its source documentation are mapped in [CP1](01-journey.md).


## Delivery 1 artifacts

| Checkpoint | Artifact | Technical validation |
|---|---|---|
| CP1 | [Journey and trace map](01-journey.md) | Components/endpoints checked against official docs; context on twelve diagram arrows |
| CP2 | [Structured log contract](02-log-contract.md) | Seven JSON examples parsed; required fields, chronology and common trace checked |
| CP3 | [Run and test guide](03-instrumentation/HOW-TO-RUN.md) | Java21 build; four automated tests passed; real HTTP error/rejection/metrics probes |
| CP4 | [Metric catalog](04-metrics.md) | Four signals per service; bounded cardinality, buckets and PromQL reviewed |
| CP5 | [Weekly status and commit evidence](../hu-status/README.md) | Commit links, execution results and integration limits recorded |

The isolated module proves trace propagation at a mocked AMQP adapter boundary and metrics export.
It does not certify live broker delivery, domain persistence, consumer processing or end-to-end notification.
Gateway and Notifications metric definitions are proposals; Attendance metrics run in the workshop probe.
The diagram has not been visually checked in GitHub, and PromQL was not executed against live Prometheus.

## Delivery observations

Work is in branch `codex/w11-observability` of the personal fork. Checkpoint publications are
separate; no published commit was amended, rebased or force-pushed. Every commit adds at most150 lines.
CP4 and this closing checkpoint were published after the scheduled October20 20:30 cutoff.
The reflection is maintained separately by the student. Moodle submission and its time are not verified.
