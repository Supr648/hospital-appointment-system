# MediCare Hospital Appointment System

MediCare is a Spring Boot 3 application for managing doctors, patients, and
appointments. Visitors can create an account, sign in, and use a private
workspace; a read-only guest demo is also available.

## Run locally

Requirements: Java 17+ and Maven 3.9+. The default local configuration uses
MySQL. Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` for a reachable database, and set
`APP_ADMIN_USERNAME` plus a unique `APP_ADMIN_PASSWORD` of at least 16
characters before starting the application. The configured administrator
account is created on first startup.

To run locally with the included H2 database instead, set
`SPRING_DATASOURCE_URL=jdbc:h2:mem:hospitaldb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`,
`SPRING_DATASOURCE_USERNAME=sa`, `SPRING_DATASOURCE_PASSWORD=` and
`SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver`.

```powershell
mvn spring-boot:run
```

Open <http://localhost:8080>. New users can register with an email address and
password; each account receives a separate workspace. Browser pages use the
sign-in form, while `/api/**` endpoints require HTTP Basic authentication.
Appointment and activity records are stored in the configured relational
database.

## Tests

Run the Maven test suite:

```powershell
mvn test
```

The automated suite covers authentication, registration, password hashing,
appointment validation, and workspace-scoped operations. It includes full
Spring Boot integration tests using an isolated in-memory H2 database; those
tests do not require MySQL.

## Run with Docker Compose

Copy `.env.example` to `.env` and replace every example password with a strong,
unique value. Then start the application and database:

```powershell
docker compose up --build -d
```

Open <http://localhost:8080>. The database service is not published to the
host; MySQL data is persisted in a named Docker volume. Set
`APP_BIND_ADDRESS=0.0.0.0` only when the host is intentionally reachable from
other machines.

## Deploy to Render

The `render.yaml` Blueprint uses a free web service and in-memory H2. Push this
project to a GitHub repository, then in Render select **New + → Blueprint**,
connect the repository and deployment branch, review the Blueprint, and deploy.
The Blueprint provisions a generated `APP_ADMIN_PASSWORD` for the configured
administrator; retrieve it from the service's environment settings and keep it
secret. Other users can create their own accounts from the welcome page.

**Important: Render Free has no persistent application disk, and this
Blueprint's H2 database is in memory. Accounts, doctors, patients, appointments,
and activity history are lost whenever the service restarts or redeploys.** Do
not rely on this deployment for day-to-day records. Before using it as a
persistent service, configure a durable managed database and backup/restore
policy, then verify that the deployed schema and data survive a restart.

This is a student/demo application, not a healthcare-compliant product. Do not
store real patient or protected health information. Email verification,
password recovery, login throttling, operational monitoring, and a healthcare
privacy/compliance review are not implemented.
