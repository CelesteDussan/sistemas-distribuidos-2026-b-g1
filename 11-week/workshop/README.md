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
Delivery1 CP4 and CP5 were published after the scheduled October20 20:30 cutoff.
The reflection is maintained separately by the student. Moodle submission and its time are not verified.

## Delivery 2 artifacts and verification

| Checkpoint | Artifact | Verification scope |
|---|---|---|
| CP6 | [SLIs, SLOs and budgets](05-slo.md) | Three exact metric queries; hypothetical school model and request/minute budget arithmetic checked |
| CP7 | [Alerts](06-alerts.yml) and [policy](06-alert-policy.md) | YAML parsed, windows/labels/math checked; no live alert evaluation |
| CP8 | [Fast-burn runbook](07-runbook.md) | Trace/PromQL diagnosis, three ordered mitigations and integrated recovery criteria reviewed |
| CP9 | [Incident and postmortem](08-incident.md) | Four syntheticJSONlogs and analyticcounts/SLI/alertoutcomes checked; no live fault injection |
| CP10 | [Updated weekly evidence](../hu-status/README.md) | D1history retained, D2commitlinks and honest limits recorded |

The slow-only asynchronous incident exposes an end-to-end delay measurement gap; availability pages
correctly remain inactive because the modeled requests succeed. The synchronous CP3probe is distinguished.
Neither this model nor proposed notification metrics certify real production reliability or error budgets.

Delivery2 requires at leastfive separate checkpoint publications, >=15minutes apart, in >=twoworkblocks;
that publication history is retained without backdating or rewriting. Each commit adds at most150lines.
The authoritative Moodle text requires submission before WednesdayOctober21 23:59-05:00; CP7onward
were published late. The earlier Thursday10:10 PDF deadline was superseded by that supplied assignment text.
The student must write CP6-CP9 reflections and a minimumone-page Implementation plan for my project
in the same Google Doc. Their authorship, completion and Moodle submission are not certified here.
Moodle D2submission is online text with fork,workshopfolder,reflection and the finalcommitURL.
