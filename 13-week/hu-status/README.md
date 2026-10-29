<!-- HU-STATUS TEMPLATE - keep markers and table headers for automated grading. -->
# Weekly Status - Week 13

<!-- CONFIG-START - must match profile CONFIG -->
- FULL_NAME: Maria Celeste Dussan Ospina
- GITHUB_USER: CelesteDussan
- TEAM: G1
- SPRINT_GOAL: Model Gateway-to-Attendance threats and define testable security backlog.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| SEC-67 | Reject non-teacher Attendance writes (E, risk 9, P0) | doing | [Issue #67](https://github.com/code-corhuila/educk-docs/issues/67) |
| SEC-68 | Scope attendance queries to authorized students (I, risk 9, P0) | todo | [Issue #68](https://github.com/code-corhuila/educk-docs/issues/68) |
| SEC-69 | Bind teacher attribution to verified principal (S, risk 9, P0) | todo | [Issue #69](https://github.com/code-corhuila/educk-docs/issues/69) |

## 2. My individual contribution
- Claimed Gateway -> Attendance inbound HTTP in [#64](https://github.com/code-corhuila/educk-docs/issues/64).
- Mapped Identity, Academic, Attendance, Communication, RabbitMQ and Notifications,
  with assets and implemented endpoint/authentication evidence in [CP1](../workshop/01-attack-surface.md).
- Opened [STRIDE PR #65](https://github.com/code-corhuila/educk-docs/pull/65) to official main,
  with S/T/R first and I/D/E/ranking/residual proposal in a second push.
- Created three labeled issues with automated acceptance criteria; controls remain unimplemented.
- Added the [CI proposal and MVC regression](../workshop/04-security-ci/HOW-TO-RUN.md).
- Actual Java 21 run: 2 tests, 1 passed, 1 failed, 0 errors. A PARENT write returned
  201 instead of 403; TEACHER control passed. No real database/broker operations ran.
- SnakeYAML 2.2 parsed security.yml; PR trigger and all three gate definitions verified.

## 3. Blockers and risks
- Physical CORHUILA library visit/loan record and book details have not been provided.
  Required page-derived [B] mitigation and acceptance criterion are still missing.
- Student-written Week 13 Google Docs reflection link and entries remain pending.
- Three issues have verified `security` and `P0` labels, but GitHub Project placement
  is unverified: API lacks project scope and browser automation could not continue.
- Fork/course histories diverge; importing upstream would exceed the workshop commit
  size limit. No synchronization or replacement of personal evidence is claimed.
- CP0/CP1/CP2 were published after their scheduled slots, starting at 19:43 local.
  Actual timestamps are retained; no retroactive class attendance or push times claimed.
- The workflow is stored as a proposal outside .github/workflows; hosted execution,
  gitleaks and dependency audit were not run. Missing authorization leaves a red test.
- Draft PR #65 is open and conflict-free as observed on October 28; approval and
  mandatory check success are not established. No merge was performed.

## 4. Plan for next week
- Complete CP6-CP9 in the same artifacts by October 30 at 23:59 America/Bogota.
- Retain the physical book citation and respond to peer review on PR #65.
- Refine the same three stories, add them to backlog and implement role/object checks
  through the team's approved environment PR workflow before reporting fixes done.
- For final delivery, make at least four additional pushes, >=15 minutes apart,
  on at least two distinct days. Current class pushes do not satisfy that future rhythm.
- Student writes reflection entries and the final half-page implementation plan,
  submits Moodle links, then prepares to explain risk scores and the failing regression.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Technical model uses real EduTrack components and scoped boundary
- [x] Testable acceptance criteria
- [x] Negative authorization test executed with real HTTP controller/security configuration
- [x] DDD / hexagonal boundaries preserved; no production code changed
- [x] No credentials or real environment files included
- [ ] Physical book and on-time library record verified
- [ ] Student reflection and Moodle submission verified
- [ ] All tests pass and GitHub Project placement verified
- [ ] Required review/checks approved; feature fixes promoted through environment PRs

## 6. Evidence links
- [Workshop](../workshop/README.md) and [STRIDE PR](https://github.com/code-corhuila/educk-docs/pull/65).
- [CP0 commit](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/9f4a6f9ea6dbfba7bed1c57ee31e10516d317427).
- [CP1 commit](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/9217844779b2cae1d7540e81ae1272cc97f57d69).
- [CP2 first commit](https://github.com/code-corhuila/educk-docs/commit/c7bd978029fe73106da08c5c9ca3737951a6955a).
- [CP2 second commit](https://github.com/code-corhuila/educk-docs/commit/03346973dfcc56de15eb63a23d3d9b9b0957cea9).
- [CP3 commit](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/1b3cd4ed2fb4e8914a2fedb948a75cd841d75385).
- [CP4 commit](https://github.com/CelesteDussan/sistemas-distribuidos-2026-b-g1/commit/f3d7f58758c158f7b5bed4f4d8d52c8050dddc69).
- [Peer review on XimenaChala PR #70](https://github.com/code-corhuila/educk-docs/pull/70#pullrequestreview-5691574507):
  row I must use the verified principal, not forgeable X-User-Id; propose a conflicting-header IDOR test.
  Submitted as COMMENT, without approving or merging.
