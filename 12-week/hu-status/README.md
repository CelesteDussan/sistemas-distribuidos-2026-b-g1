<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       12-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 12

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Maria Celeste Dussan Ospina
- GITHUB_USER: CelesteDussan
- TEAM: G1
- SPRINT_GOAL: Audit EduTrack integration readiness, focusing on portal PRs, cross-service dependencies, and the evidence still needed for real end-to-end acceptance.
<!-- CONFIG-END -->

## 1. User stories worked this week

On October 25, I reviewed the state of 19 official project repositories and recorded the status of open portal work. This was a delivery and integration review, not implementation of the portal PRs listed below. Their status is historical as of that review; none is reported as complete based on this audit.

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-001 | View grades in near real time — portal integration review | doing | [Academic Portal PR #7 — design tokens](https://github.com/code-corhuila/educk-academic-portal/pull/7), [PR #8 — mobile layout](https://github.com/code-corhuila/educk-academic-portal/pull/8) · [project status review](https://github.com/code-corhuila/educk-docs/commit/59963a92e2618c73c7b4561016ec22157efecb56) |
| HU-003 | Sign in and use the Identity portal — integration review | doing | [Identity Portal PR #13 — design tokens](https://github.com/code-corhuila/educk-identity-portal/pull/13), [PR #14 — shared client](https://github.com/code-corhuila/educk-identity-portal/pull/14), [PR #15 — responsive login](https://github.com/code-corhuila/educk-identity-portal/pull/15) · [project status review](https://github.com/code-corhuila/educk-docs/commit/59963a92e2618c73c7b4561016ec22157efecb56) |
| HU-004 | Direct communication with teachers — portal integration review | doing | [Communication Portal PR #10 — design tokens](https://github.com/code-corhuila/educk-communication-portal/pull/10), [PR #11 — mobile layout](https://github.com/code-corhuila/educk-communication-portal/pull/11), [PR #19 — controller/federation](https://github.com/code-corhuila/educk-communication-portal/pull/19) · [project status review](https://github.com/code-corhuila/educk-docs/commit/59963a92e2618c73c7b4561016ec22157efecb56) |
| HU-005 | Register attendance with causal ordering — portal integration review | doing | [Attendance Portal PR #7 — design tokens](https://github.com/code-corhuila/educk-attendance-portal/pull/7), [PR #8 — shared client](https://github.com/code-corhuila/educk-attendance-portal/pull/8), [PR #14 — alias/tracing](https://github.com/code-corhuila/educk-attendance-portal/pull/14) · [project status review](https://github.com/code-corhuila/educk-docs/commit/59963a92e2618c73c7b4561016ec22157efecb56) |

## 2. My individual contribution

- Authored a cross-repository delivery review covering 19 official repositories, including the shell and four domain portals.
- Recorded 11 open portal PRs across Academic, Identity, Attendance, and Communication, separating existing implementation from approval, integration, and runtime acceptance.
- Identified that a complete real-service user journey had not been established and documented concrete portal, backend, infrastructure, and contract dependencies for follow-up.
- Did not run tests or a full runtime deployment for the review; historical CI links in the report were identified as historical evidence, not new validation.

## 3. Blockers and risks

- The portal PRs were open at the October 25 observation; the review did not verify approval or merge readiness for each PR.
- No fresh browser acceptance or complete real-service journey was run for this review.
- Communication MongoDB provisioning/credentials and end-to-end notification ownership remained unresolved in the reviewed project state.

## 4. Plan for next week

- Recheck the portal PRs and integrate only reviewed changes through the required workflow.
- Run browser acceptance against the integrated shell and real services, including login/session handoff, grade loading, attendance persistence, and Communication navigation.
- Update HU states only after approvals, required checks, and acceptance evidence are verified.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

This week’s verified contribution was the delivery review. It did not implement portal code, rerun application tests, or claim end-to-end acceptance.

## 6. Evidence links

- [Project-wide delivery review commit](https://github.com/code-corhuila/educk-docs/commit/59963a92e2618c73c7b4561016ec22157efecb56)
- [Project-wide delivery review PR #62](https://github.com/code-corhuila/educk-docs/pull/62)
- [Project status report](https://github.com/code-corhuila/educk-docs/blob/docs/project-status-review-2026-10-25/11-quality/project-status-2026-10-25.md)
- [Course repository Week 12 status file](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/blob/main/12-week/hu-status/README.md)
