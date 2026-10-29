# Week 13 - EduTrack threat modeling

<!-- CONFIG-START -->
- FULL_NAME: Maria Celeste Dussan Ospina
- GITHUB_USER: CelesteDussan
- TEAM: G1
- DOCS_REPO: https://github.com/code-corhuila/educk-docs
- TRUST_BOUNDARY: API Gateway -> Attendance API, inbound HTTP
- CLAIM_ISSUE: https://github.com/code-corhuila/educk-docs/issues/64
- REFLECTION_DOC: pending student-created Week 13 document
- LIBRARY_BOOK: pending verified physical-library consultation details
- LIBRARY_RECORD_TIME: pending actual CORHUILA library record
- STORIES: pending CP3
<!-- CONFIG-END -->

## Scope

The boundary covers `educk-api-gateway` forwarding attendance writes and reads
to `educk-attendance-api`; `StudentAbsent` is a possible downstream effect.
Attendance-to-PostgreSQL and Attendance-to-RabbitMQ are separate boundaries.
The claim issue was opened on October 28 at 19:42:24 America/Bogota.
This is later than the scheduled CP0 slot; publication times remain unmodified.

## Evidence and open requirements

- [Boundary claim](https://github.com/code-corhuila/educk-docs/issues/64).
- Official docs were fetched before creating `security/stride-CelesteDussan` from main.
- The personal branch is `codex/w13-threat-modeling`; prior local work is preserved.
- Upstream course main was fetched, but fork synchronization remains pending:
  the histories diverge and importing the upstream changes exceeds the workshop's
  150-added-line limit. No personal evidence was overwritten.
- The supplied Spanish PDF omits the full English CONFIG template; these fields
  include the explicitly required book, visit time, stories and identity entries.
- CP0 is incomplete until the student creates/shares the reflection document.
- Library consultation and reflection entries require the student's real evidence.
- All technical artifacts are in English. Checkpoints are published separately.
