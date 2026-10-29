# Attendance authorization regression

Story: [#67](https://github.com/code-corhuila/educk-docs/issues/67), STRIDE E, risk 9.
Criterion: a PARENT cannot POST `/api/v1/attendance`; expect 403 and no write use-case invocation.

Use Java 21 and Maven 3.9. Check out `educk-attendance-api` at
`8b1cf52501a6b994a5adb2c3918591e28a80684d` into a disposable local folder.
Create `src/test/java/com/corhuila/edutrack/attendance/infrastructure/web/` there
and copy `AttendanceAuthorizationTest.java` into it. From that service folder:

```sh
mvn -B -Dtest=AttendanceAuthorizationTest test
```

This MVC slice uses the real AttendanceController, SecurityConfig and JWT filter.
Only JWT decoding and use-case ports are mocked. `fixture` is a synthetic provider
stub, not a real bearer credential. No database or RabbitMQ is started.
A teacher control request must reach the write use case; a blanket-deny policy
would fail that control test. School assignment and token cryptography are outside this slice.

The negative case is expected to expose the current missing role rule; do not
weaken its 403 assertion or mark the security story done if it fails.
Actual run, October 28 at 19:51 America/Bogota, Java 21.0.12.1 / Maven 3.9.6:

```text
AttendanceAuthorizationTest.parentCannotPostAttendance:52 Status expected:<403> but was:<201>
Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
BUILD FAILURE
```

The TEACHER control passed. The PARENT request returned 201, confirming the role
gap in this MVC slice. Mocked use cases mean no real student write occurred.
The initial sandbox run stalled during Mockito agent attachment; the isolated
retry completed. The full console log remains local at `tmp/w13-negative-test-escalated.log`.

`security.yml` is a proposal stored here as the workshop requires. GitHub Actions
does not execute workflows from this folder. To trial it, the team must place a copy
under `.github/workflows/` in an authorized test branch, provision a read-only
Attendance checkout secret. The proposed workflow creates the test destination.
The dependency audit requires network access and a current vulnerability database.
No hosted run URL or successful audit is claimed before an observed run.
Validation: SnakeYAML 2.2 parsed this YAML; PR trigger and all three gates passed structural checks.
