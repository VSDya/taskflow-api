# TaskFlow API

Production-style REST API for managing projects and tasks.

> **Portfolio project:** demonstrates a realistic Java backend workflow from requirements and data modelling through implementation, testing, containerization and CI.

## Why this project

TaskFlow is designed as a small but realistic backend service rather than a simple CRUD demo. It demonstrates API design, validation, persistence, database migrations, automated testing and containerized development.

## Tech stack

- Java 21
- Spring Boot 3
- Spring Web + Validation
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- JUnit 5 + Mockito
- Testcontainers
- Docker Compose
- GitHub Actions

## Features

- Project CRUD
- Task CRUD
- Project → task relationship
- Task status: `TODO`, `IN_PROGRESS`, `DONE`
- Task priority: `LOW`, `MEDIUM`, `HIGH`
- Pagination and sorting
- Request validation
- Consistent `404` API errors
- PostgreSQL persistence
- Versioned database migrations
- Unit and integration tests
- CI pipeline on GitHub Actions

## API

### Projects

```text
POST   /api/v1/projects
GET    /api/v1/projects
GET    /api/v1/projects/{id}
PUT    /api/v1/projects/{id}
DELETE /api/v1/projects/{id}
```

### Tasks

```text
POST   /api/v1/projects/{projectId}/tasks
GET    /api/v1/projects/{projectId}/tasks
GET    /api/v1/tasks/{id}
PUT    /api/v1/tasks/{id}
DELETE /api/v1/tasks/{id}
```

List endpoints support Spring pagination and sorting parameters such as `page`, `size` and `sort`.

## Architecture

```text
HTTP / REST
     ↓
Controllers
     ↓
Application Services
     ↓
Domain / DTOs
     ↓
Repositories
     ↓
JPA / Hibernate
     ↓
PostgreSQL
```

DTOs are used at the API boundary. Database schema changes are managed by Flyway. Integration tests use a real PostgreSQL container through Testcontainers.

## Run locally

### Requirements

- JDK 21
- Maven 3.9+
- Docker Desktop
- Git

### Start PostgreSQL

```bash
docker compose up -d
```

### Run tests

```bash
mvn test
```

Integration tests require Docker because Testcontainers starts PostgreSQL automatically.

### Start the API

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

Health check:

```text
GET /api/v1/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "taskflow-api"
}
```

## Next production improvements

The next planned improvements are authentication, authorization, OpenAPI documentation and additional production hardening such as rate limiting and observability.

## Project status

🟢 Core Project and Task APIs are implemented.

🚧 Authentication, authorization, OpenAPI documentation and additional production hardening are planned next.
