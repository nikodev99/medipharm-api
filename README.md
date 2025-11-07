# Medipharm — Backend (API)

> **Medipharm (backend)** — Reactive API service for the Medipharm project.

---

## Table of contents

- [Project overview](#project-overview)
- [Tech stack](#tech-stack)
- [Repository layout](#repository-layout)
- [Prerequisites](#prerequisites)
- [Setup & running locally](#setup--running-locally)
- [Environment variables](#environment-variables)
- [Database (R2DBC)](#database-r2dbc)
- [Build & CI suggestions](#build--ci-suggestions)
- [API documentation](#api-documentation)
- [Testing](#testing)
- [Development tips](#development-tips)
- [Contributing](#contributing)
- [License](#license)

---

## Project overview

This repository contains the **backend API only** — a reactive, non-blocking Spring Boot WebFlux service written in Kotlin. It exposes REST (and/or reactive endpoints) for pharmacy workflows: patients, prescriptions, inventory, suppliers, billing and reporting. This project uses Gradle (Kotlin DSL) as the build tool and R2DBC for reactive database access.

## Tech stack

- Kotlin
- Spring Boot (WebFlux)
- Gradle (Kotlin DSL)
- Spring Security (JWT or your chosen reactive auth)
- Spring Data R2DBC (Postgres driver)
- Database migrations: use a reactive-aware migration tool (e.g., `r2dbc-migrate`) or run Flyway/Liquibase on a JDBC connection during CI/deploy if preferred.
- Testcontainers (only for integration tests that need real DB; can be used with JDBC adapter or r2dbc-test support)

> **Note:** This project intentionally does not use JPA/Hibernate and does not use Docker or Kubernetes.

## Repository layout

```
medipharm-backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/                       # wrapper
├── src/main/kotlin/
├── src/main/resources/
├── src/test/
└── README.md
```

## Prerequisites

- Java 17+ (or the Java version your Spring Boot version requires)
- Gradle wrapper (the repo contains `gradlew`)
- PostgreSQL (or another R2DBC-supported DB)

## Setup & running locally

1. Clone the repo and open the project in your IDE.

```bash
git clone https://github.com/<your-org>/medipharm-backend.git
cd medipharm-backend
```

2. Configure environment variables (see next section) or place them in `application-local.yml` (do NOT commit secrets).

3. Run the app locally with Gradle:

```bash
# default profile
./gradlew bootRun

# or with a profile
./gradlew bootRun -Dspring-boot.run.profiles=local
```

4. Build and run the jar:

```bash
./gradlew clean bootJar
java -jar build/libs/medipharm-backend-0.0.1.jar --spring.profiles.active=local
```

## Environment variables

Put secrets in an untracked `.env` file or configure them in your environment. Example variables:

```env
SPRING_PROFILES_ACTIVE=local
SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/medipharm
SPRING_R2DBC_USERNAME=medipharm
SPRING_R2DBC_PASSWORD=change_me
JWT_SECRET=replace-with-strong-secret

# Optional mail / external services
MAIL_HOST=smtp.example.com
```

If you prefer YAML profiles, create `application-local.yml` and add the R2DBC url and credentials there.

## Database (R2DBC)

- Use `spring-boot-starter-data-r2dbc` and the official `r2dbc-postgresql` driver.
- For schema migrations, choose a reactive-compatible tool such as `r2dbc-migrate`, or run Flyway/Liquibase during CI using a standard JDBC connection if you accept a non-reactive migration step at deploy time.
- Keep SQL migration scripts under a `migrations/` directory so they can be run by your chosen migration tool.

## Build & CI suggestions

- Use Gradle GitHub Actions workflow to run `./gradlew clean build` and tests.
- Store secrets in GitHub Actions secrets or your CI secret manager.
- Optionally build and publish artifacts (JAR) to your internal artifact registry.

## API documentation

- Keep API documentation minimal and inline with controllers (DTOs). If you want OpenAPI, add `springdoc-openapi-webflux` to generate a spec and UI.
- Otherwise keep a small `openapi.yaml` in the repo root if you prefer manual docs.

## Testing

- Unit tests: JUnit 5 + MockK are recommended for Kotlin.
- Integration tests: consider `Testcontainers` with a real Postgres instance — you can use an adapter for R2DBC or start a JDBC Postgres container and run migrations before tests.

Run tests with:

```bash
./gradlew clean test
```

## Development tips

- Use `spring-boot-devtools` for faster restart in development if desired.
- Favor immutable DTOs and small services in the reactive stack. Keep blocking code out of reactive chains.
- Use `WebTestClient` for WebFlux controller tests.
- Centralize configuration in `application.yml` and use profile-specific files for local/prod.

## Contributing

1. Fork or branch from `main`.
2. Follow repo Kotlin style and run `./gradlew check`.
3. Add tests and a clear PR description.

## License

Add your preferred license (e.g., MIT, Apache-2.0) in a `LICENSE` file.

---

If you want, I can now:
- replace the README in the repo with this reactive version (done),
- add a sample `application-local.yml` tuned to your existing `application.yml` you previously shared,
- add a `build.gradle.kts` proofing snippet that matches the libraries you already provided,
- scaffold a simple GitHub Actions workflow for Gradle.

Pick one and I'll add it directly.

