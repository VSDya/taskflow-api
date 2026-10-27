# TaskFlow API

A Java 21 / Spring Boot REST API for project and task management, with JWT authentication, owner-scoped authorization, PostgreSQL persistence, Flyway migrations, OpenAPI documentation, and automated tests.

Русская версия: [README.ru.md](README.ru.md).

> **Portfolio project:** demonstrates API design, authentication, authorization, relational data modelling, integration testing, containerization, and CI.

## Tech stack

- Java 21, Spring Boot 3
- Spring Web, Validation, Spring Security
- Spring Data JPA / Hibernate, PostgreSQL, Flyway
- JWT (JJWT), BCrypt password hashing
- OpenAPI / Swagger UI
- JUnit 5, Mockito, Testcontainers
- Docker Compose, GitHub Actions

## Features

- User registration and login with signed JWT access tokens
- BCrypt password hashing
- Owner-scoped project CRUD and task operations
- Task status: `TODO`, `IN_PROGRESS`, `DONE`
- Task priority: `LOW`, `MEDIUM`, `HIGH`
- Request validation, structured API errors, pagination and sorting
- Database schema managed by Flyway
- Unit, MVC, and PostgreSQL integration tests
- CI build and verification through GitHub Actions

## API

### Authentication

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a user |
| POST | `/api/v1/auth/login` | Obtain a JWT |

### Projects

| Method | Endpoint |
| --- | --- |
| POST | `/api/v1/projects` |
| GET | `/api/v1/projects` |
| GET | `/api/v1/projects/{id}` |
| PUT | `/api/v1/projects/{id}` |
| DELETE | `/api/v1/projects/{id}` |

### Tasks

| Method | Endpoint |
| --- | --- |
| POST | `/api/v1/projects/{projectId}/tasks` |
| GET | `/api/v1/projects/{projectId}/tasks` |
| GET | `/api/v1/tasks/{id}` |
| PUT | `/api/v1/tasks/{id}` |
| DELETE | `/api/v1/tasks/{id}` |

Protected endpoints require `Authorization: Bearer <token>`. List endpoints support `page`, `size` (maximum 100), and `sort`.

OpenAPI UI is available at `/swagger-ui/index.html` when the application is running.

## Run locally

### Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop / Docker Engine with Compose
- OpenSSL (for generating a secret)

### Configure environment

Copy the example file and replace the placeholders before starting the services:

```bash
cp .env.example .env
```

Generate a strong JWT signing secret:

```bash
openssl rand -base64 48
```

Put the generated value in `JWT_SECRET` in your local `.env` file. Replace the database password placeholder with a strong password too. **Do not commit `.env` or share its contents.** The application intentionally has no default JWT secret and will fail to start if `JWT_SECRET` is missing or too short for the configured HMAC algorithm.

### Start the API and PostgreSQL

```bash
docker compose up --build -d
```

The API listens on `http://localhost:8080` and is bound to loopback by default.

### Run tests

```bash
mvn verify
```

Integration tests use Testcontainers and require Docker. They start their own PostgreSQL containers; they do not require a manually running local PostgreSQL instance.

### Health endpoint

```text
GET /api/v1/health
```

This endpoint is public and does not require a bearer token.

## Architecture

```text
HTTP / REST
     ↓
Controllers / DTO validation
     ↓
Application services + ownership checks
     ↓
Repositories
     ↓
JPA / Hibernate
     ↓
PostgreSQL (Flyway migrations)
```

## Known production-hardening work

This is a portfolio project, not a claim of production readiness. Before production use, consider rate limiting and abuse protection for authentication, centralized secret management and rotation, observability/metrics, deployment-specific health checks, and a documented backup/restore process.
