# TaskFlow API

A REST API for project and task management, built with Java 21 and Spring Boot 3. It demonstrates JWT authentication, owner-scoped authorization, PostgreSQL persistence, Flyway migrations, API documentation, automated tests, Docker Compose, and CI.

**Documentation:** [Русская версия — README.ru.md](README.ru.md)

> **Portfolio project.** This repository demonstrates backend API design and testing practices. It is not presented as production-ready software.

## Contents

- [Features](#features)
- [Technology stack](#technology-stack)
- [Requirements](#requirements)
- [Run locally](#run-locally)
- [API reference](#api-reference)
- [Pagination and sorting](#pagination-and-sorting)
- [Error responses](#error-responses)
- [Architecture and migrations](#architecture-and-migrations)
- [Testing and CI](#testing-and-ci)
- [Production hardening](#production-hardening)

## Features

- User registration and login with signed JWT access tokens
- Password hashing with BCrypt
- CRUD operations for projects and tasks
- Owner-scoped access: users can access only their own projects and tasks
- Task statuses: `TODO`, `IN_PROGRESS`, `DONE`
- Task priorities: `LOW`, `MEDIUM`, `HIGH`
- Request validation and structured JSON errors
- Pagination and sorting for list endpoints
- Database schema migrations managed by Flyway
- Unit, MVC, and PostgreSQL integration tests
- OpenAPI documentation and Swagger UI
- Docker Compose development environment and GitHub Actions CI

## Technology stack

- Java 21, Spring Boot 3
- Spring Web, Spring Validation, Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL 16, Flyway
- JJWT, BCrypt
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito, MockMvc, Testcontainers
- Maven, Docker Compose, GitHub Actions

## Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop or Docker Engine with Docker Compose
- OpenSSL (optional, for generating a JWT secret)

## Run locally

### 1. Configure environment variables

Create a local environment file from the example:

```bash
cp .env.example .env
```

On Windows PowerShell, use:

```powershell
Copy-Item .env.example .env
```

Edit `.env` and replace the `POSTGRES_PASSWORD` placeholder with a strong database password. Generate a JWT signing secret with:

```bash
openssl rand -base64 48
```

Put the generated value in `JWT_SECRET`. The application intentionally has no default JWT secret and will fail to start if the secret is missing or too short for the configured HMAC algorithm. **Do not commit `.env` or share its contents.**

### 2. Build the application JAR

The Dockerfile copies the JAR from `target/`, so build it before the first Docker Compose startup and after changing application code:

```bash
mvn -B package
```

### 3. Start the API and PostgreSQL

```bash
docker compose up --build -d
```

The API is available at `http://localhost:8080`. Both application and PostgreSQL ports are bound to loopback by default.

Useful URLs:

- Health check: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health) — public; no token required
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Stop the containers while preserving the database volume:

```bash
docker compose down
```

**Data warning:** `docker compose down -v` removes the named PostgreSQL volume and deletes the local database data. Use it only when you intentionally want to remove that data.

### 4. Run with Maven (optional)

For local development, start PostgreSQL first:

```bash
docker compose up -d postgres
```

Set `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, and `JWT_SECRET` in the current shell, making sure the database credentials match the running PostgreSQL instance. Then run:

```bash
mvn spring-boot:run
```

## API reference

All endpoints use the `/api/v1` base path.

### Authentication

| Method | Endpoint | Description | Success |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a user | `201 Created` |
| POST | `/api/v1/auth/login` | Log in and obtain a JWT | `200 OK` |

Example registration or login body:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

Passwords must contain 8–72 characters. Registration creates the account but does not return a JWT; log in to obtain a token. The default token lifetime is one hour.

### Projects

All project endpoints require JWT authentication.

| Method | Endpoint | Description | Success |
| --- | --- | --- | --- |
| POST | `/api/v1/projects` | Create a project | `201 Created` |
| GET | `/api/v1/projects` | List the current user's projects | `200 OK` |
| GET | `/api/v1/projects/{id}` | Get a project | `200 OK` |
| PUT | `/api/v1/projects/{id}` | Update a project | `200 OK` |
| DELETE | `/api/v1/projects/{id}` | Delete a project | `204 No Content` |

### Tasks

All task endpoints require JWT authentication.

| Method | Endpoint | Description | Success |
| --- | --- | --- | --- |
| POST | `/api/v1/projects/{projectId}/tasks` | Create a task in a project | `201 Created` |
| GET | `/api/v1/projects/{projectId}/tasks` | List a project's tasks | `200 OK` |
| GET | `/api/v1/tasks/{id}` | Get a task | `200 OK` |
| PUT | `/api/v1/tasks/{id}` | Update a task | `200 OK` |
| DELETE | `/api/v1/tasks/{id}` | Delete a task | `204 No Content` |

Send the token in the `Authorization` header for protected endpoints:

```http
Authorization: Bearer <JWT>
```

In Swagger UI, click **Authorize** and enter the token itself; Swagger UI adds the `Bearer` prefix.

### Health check

| Method | Endpoint | Description | Success |
| --- | --- | --- | --- |
| GET | `/api/v1/health` | Check API availability | `200 OK` |

The health endpoint is public and does not require a JWT.

## Pagination and sorting

List endpoints support these query parameters:

| Parameter | Default | Description |
| --- | --- | --- |
| `page` | `0` | Zero-based page index |
| `size` | `20` | Page size, capped at 100 |
| `sort` | Endpoint-dependent | Sort property and direction |

Example:

```http
GET /api/v1/projects?page=0&size=20&sort=createdAt,desc
```

List endpoints return the same pagination response structure for projects and tasks.

Example response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

This example is illustrative. `content` contains the items on the current page; the remaining fields describe the page and the total number of matching items.

| Field | Description |
| --- | --- |
| `content` | Items on the current page |
| `page` | Zero-based index of the current page |
| `size` | Page size, capped at 100 |
| `totalElements` | Total number of matching items |
| `totalPages` | Total number of pages |
| `first` | Whether this is the first page |
| `last` | Whether this is the last page |

An unknown sort property returns `400 Bad Request` with the `INVALID_SORT_PROPERTY` error code.

## Error responses

API errors use a consistent JSON structure. Example:

```json
{
  "status": 404,
  "error": "PROJECT_NOT_FOUND",
  "message": "Project not found",
  "timestamp": "2026-10-26T12:00:00Z"
}
```

Common error codes:

| HTTP status | Error code | Meaning |
| --- | --- | --- |
| `400 Bad Request` | `VALIDATION_ERROR` | Request validation failed |
| `400 Bad Request` | `INVALID_SORT_PROPERTY` | Unknown sort property |
| `400 Bad Request` | `MALFORMED_JSON` | Request body is malformed or unreadable |
| `400 Bad Request` | `INVALID_PARAMETER_TYPE` | A path or request parameter has an invalid type |
| `401 Unauthorized` | `UNAUTHORIZED` | Authentication is missing or the JWT is invalid |
| `401 Unauthorized` | `INVALID_CREDENTIALS` | Email or password is incorrect |
| `404 Not Found` | `PROJECT_NOT_FOUND` | Project does not exist or is not accessible to the user |
| `404 Not Found` | `TASK_NOT_FOUND` | Task was not found |
| `409 Conflict` | `USER_ALREADY_EXISTS` | An account with that email already exists |

Access to another user's project or task is treated as `404 Not Found`.

## Architecture and migrations

```text
HTTP / REST
     ↓
Controllers and DTO validation
     ↓
Application services and ownership checks
     ↓
Repositories
     ↓
JPA / Hibernate
     ↓
PostgreSQL (Flyway migrations)
```

Flyway SQL migrations are stored in `src/main/resources/db/migration`. Hibernate validates the mapped schema; Flyway manages schema changes.

## Testing and CI

Run the full verification suite:

```bash
mvn -B verify
```

The integration tests use Testcontainers to start their own PostgreSQL containers, so a separately running local PostgreSQL instance is not required. Docker must be running for these tests.

GitHub Actions validates the Docker Compose configuration and runs Maven verification and packaging. Workflow file: `.github/workflows/ci.yml`.

## Production hardening

This is a portfolio project, not a claim of production readiness. Before production use, consider rate limiting and abuse protection for authentication, centralized secret management and rotation, observability and metrics, deployment-specific health checks, and a documented backup/restore process.
