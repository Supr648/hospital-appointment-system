# Runtime Validation Report

**Generated**: 2026-10-12
**Target**: Hospital Appointment System

## Summary

| Step | Status | Exit Code | Details |
|---|---|---:|---|
| Startup | PASS | n/a | `mvn spring-boot:run` with isolated H2 on port 18765; startup completed in 10.6s and `GET /` returned 200. |
| Integration tests | PASS | 0 | `mvn -q test`: 30 tests passed, 0 failed, 0 errors, 0 skipped. |
| Browser flows | PASS | n/a | Interactive Chromium flow covered registration, sign-in, doctor/patient creation, appointment booking/read-back, and workspace separation. |

**Overall**: PASS for the tested local H2 configuration. This does not make the
current Render Free deployment suitable for durable day-to-day records.

## Environment and capability decisions

- Docker: **UNAVAILABLE** — `docker info` reported that the Docker daemon was
  not responding.
- Node.js: **AVAILABLE** — v24.19.0.
- Playwright: **AVAILABLE** — v1.64.0; Chromium installation completed.
- Infrastructure tier: **FALLBACK (embedded/in-memory)** — used H2 because
  Docker was unavailable. Gap: this does not validate MySQL-specific behavior
  or production database durability.
- Browser tier: **PRIMARY (Chromium)** — exercised the running server-rendered
  application independently of the Docker limitation.

## Flows exercised

1. Registered an account, signed in with the new credentials, reached the
   protected dashboard, and confirmed anonymous dashboard access redirects to
   sign-in. A wrong password was rejected.
2. Created a doctor and patient, booked a future appointment, and verified the
   appointment and activity record were visible after the write.
3. Registered a second account and verified its doctor directory did not
   contain the first account's doctor. An API appointment attempt referencing
   the first account's patient was rejected.

The browser flows were executed interactively against the local application;
the automated full-context equivalents are in
`src/test/java/com/hospital/appointmentsystem/RegistrationWorkspaceIntegrationTest.java`.
The browser interactions are not a checked-in Playwright test suite.

## Test evidence

- Maven command: `mvn -q test`
- Exit code: 0
- Surefire reports: `target/surefire-reports/`
- Full-context test database: isolated in-memory H2

## Remaining deployment limitation

The Render Blueprint currently uses Render Free with in-memory H2. All accounts
and records are discarded when the app process restarts or redeploys. Configure
a durable managed database and a tested backup/restore policy before relying on
this app for day-to-day records. Do not use it to store real patient or
protected health information.
