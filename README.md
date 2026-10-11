# Hospital Appointment System

Spring Boot 3 application for managing doctors, patients, and appointments.

## Run locally

Requirements: Java 17+ and Maven 3.9+; a reachable MySQL database is required.

Set `APP_ADMIN_USERNAME` and a unique `APP_ADMIN_PASSWORD` of at least 16
characters before starting the application. Browser pages use the sign-in form;
`/api/**` endpoints require HTTP Basic authentication. Never reuse the database
password as the application password.

Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` for your MySQL instance. Appointment activity is
stored in the same database. Then run:

```powershell
mvn spring-boot:run
```

The web app listens on port 8080 by default. Set `PORT` to change it.

## Tests

Run the controller regression suite with:

```powershell
mvn test
```

The tests use mocked persistence dependencies and do not require running MySQL.

## Run with Docker Compose

Copy `.env.example` to `.env`, replace every example password with a strong
unique value. Then start the application and its database:

```powershell
docker compose up --build -d
```

Open <http://localhost:8080>. The database service is not published to the
host; MySQL data is persisted in a named Docker volume. Set
`APP_BIND_ADDRESS=0.0.0.0` only when the host is intentionally reachable from
other machines.

## Deploy to Render

The `render.yaml` Blueprint describes a free Docker web service and a generated
administrator password. Push this project to a GitHub repository, then in
Render select **New + → Blueprint**, connect that repository, and deploy the
Blueprint. The free instance may sleep when idle and has no persistent disk.
Its in-memory H2 database is temporary: appointments and activity data are
cleared when the service restarts or redeploys. Copy the generated
`APP_ADMIN_PASSWORD` from the service's environment settings after provisioning.
This single-administrator starter is not a substitute for managed identity,
access auditing, TLS configuration, or healthcare compliance review. Do not
store real patient information until those controls are in place.

Authentication currently uses one in-memory administrator account. This is a
deployment starter, not a complete production identity system: add managed
identity/SSO, per-user authorization and audit controls, TLS, backups, and
applicable healthcare compliance controls before exposing the service publicly
or storing real patient information.
