# TaskFlow API

Production-style REST API for managing projects and tasks.

> Portfolio project focused on backend development, clean architecture and practical API design.

## Planned stack

- Java 21
- Spring Boot
- Spring Security + JWT
- PostgreSQL
- Spring Data JPA / Hibernate
- Flyway
- OpenAPI / Swagger
- JUnit 5 + Mockito
- Testcontainers
- Docker Compose
- GitHub Actions

## Core features

- User registration and authentication
- JWT-based authorization
- Projects and tasks
- Roles and permissions
- Task status, priority and deadlines
- Pagination, filtering and sorting
- Request validation
- Consistent API error responses
- Database migrations
- Automated tests
- Containerized local development

## Architecture

The application will follow a layered architecture with clear separation between API, application/business logic, persistence and domain models.

```text
HTTP / REST
    ↓
Controllers
    ↓
Services
    ↓
Repositories
    ↓
PostgreSQL
```

DTOs will be used at the API boundary, with validation and centralized exception handling.

## Local development

The project will provide Docker Compose for PostgreSQL and a reproducible development environment. Secrets and environment-specific configuration will stay outside Git.

## API documentation

Swagger / OpenAPI documentation will be available when the application is running.

## Project status

🚧 In active development.

The goal is to demonstrate how I approach a realistic backend task from requirements and data modelling through implementation, testing, containerization and documentation.
