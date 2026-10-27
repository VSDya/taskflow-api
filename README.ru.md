# TaskFlow API

REST API для управления проектами и задачами, разработанный на Java 21 и Spring Boot.

TaskFlow демонстрирует практики backend-разработки: JWT-аутентификацию, проверку владельца ресурсов, хранение данных в PostgreSQL, миграции базы данных, автоматизированное тестирование, документацию API и контейнеризацию.

## Возможности

- Регистрация пользователей и вход в систему
- Хеширование паролей с помощью BCrypt
- Stateless-аутентификация на основе JWT
- CRUD-операции для проектов
- CRUD-операции для задач
- Связь проектов с задачами
- Проверка владельца проектов и задач
- Валидация входящих запросов
- Единый формат JSON-ошибок
- Пагинация и сортировка
- Хранение данных в PostgreSQL
- Миграции схемы базы данных с помощью Flyway
- Unit- и интеграционные тесты
- Документация OpenAPI и интерфейс Swagger UI
- Среда разработки на Docker Compose
- CI через GitHub Actions

## Технологический стек

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Validation
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- JJWT
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito и MockMvc
- Testcontainers
- Maven
- Docker Compose
- GitHub Actions

## Требования

Для запуска проекта необходимы:

- JDK 21
- Maven 3.9 или новее
- Docker Desktop с поддержкой Docker Compose
- Git

## Запуск проекта

### Вариант 1. Запуск через Docker Compose

Сначала создай локальный файл конфигурации из шаблона:

```powershell
Copy-Item .env.example .env
```

Открой `.env` и замени значения `POSTGRES_PASSWORD` и `JWT_SECRET`.

- `POSTGRES_PASSWORD` — надёжный пароль PostgreSQL.
- `JWT_SECRET` — случайный секрет длиной не менее 32 байт. Его можно сгенерировать командой:

```powershell
openssl rand -base64 48
```

Не публикуй `.env` и не добавляй реальные секреты в Git.

Запусти API и PostgreSQL:

```powershell
docker compose up --build -d
```

Посмотреть логи приложения:

```powershell
docker compose logs -f app
```

После запуска приложение будет доступно по адресам:

- API: http://localhost:8080
- Health check: http://localhost:8080/api/v1/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Health endpoint доступен без токена. Остальные защищённые endpoint требуют JWT Bearer token.

Остановить контейнеры, сохранив данные PostgreSQL:

```powershell
docker compose down
```

Данные PostgreSQL сохраняются в Docker volume. Команда `docker compose down -v` удаляет volume с локальной базой данных и находящиеся в ней данные. Используй её только если действительно хочешь удалить базу.

### Вариант 2. Запуск через Maven

Этот вариант предназначен для локальной разработки. Сначала настрой переменные окружения `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` и `JWT_SECRET` в текущей оболочке. Значения пароля PostgreSQL должны совпадать с настройками запущенной базы данных.

Запусти PostgreSQL:

```powershell
docker compose up -d postgres
```

Затем запусти приложение:

```powershell
mvn spring-boot:run
```

Приложение использует параметры подключения к базе данных и JWT из переменных окружения. Пароль PostgreSQL и JWT secret обязательны; значения по умолчанию для этих секретов не предусмотрены.

English version: [README.md](README.md).

## Тестирование

Для запуска полного набора тестов выполни:

```powershell
mvn -B verify
```

Интеграционные тесты используют Testcontainers и PostgreSQL. Перед запуском тестов Docker Desktop должен работать, чтобы Testcontainers мог создать контейнер базы данных.

Тестовый набор проверяет основные сценарии аутентификации, доступ к защищённым endpoint, принадлежность ресурсов пользователям, единый формат ошибок и поведение пагинации.

## Документация API

Интерактивная документация Swagger UI:

http://localhost:8080/swagger-ui/index.html

Спецификация OpenAPI:

http://localhost:8080/v3/api-docs

Для вызова защищённых endpoint через Swagger используй кнопку `Authorize`.

Получи JWT через `/api/v1/auth/login`, затем вставь полученный токен в поле Bearer Authentication. Вставляй только сам токен, без префикса `Bearer ` — Swagger добавит его самостоятельно.

## Аутентификация

### Регистрация

`POST /api/v1/auth/register`

Пример запроса:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

Пароль должен содержать от 8 до 72 символов. Регистрация создаёт учётную запись, но не возвращает JWT. Для получения токена необходимо выполнить вход.

### Вход

`POST /api/v1/auth/login`

Пример запроса:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

При успешном входе API возвращает подписанный JWT. Для работы с защищёнными endpoint передавай токен в заголовке `Authorization`:

```http
Authorization: Bearer <JWT>
```

Стандартный срок действия токена — один час.

## API Endpoints

### Аутентификация

| Метод | Endpoint | Описание | Успешный ответ |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Регистрация пользователя | 201 Created |
| POST | `/api/v1/auth/login` | Вход и получение JWT | 200 OK |

### Проекты

Все endpoints проектов требуют JWT-аутентификации.

| Метод | Endpoint | Описание | Успешный ответ |
|---|---|---|---|
| POST | `/api/v1/projects` | Создать проект | 201 Created |
| GET | `/api/v1/projects` | Получить список своих проектов | 200 OK |
| GET | `/api/v1/projects/{id}` | Получить проект по ID | 200 OK |
| PUT | `/api/v1/projects/{id}` | Обновить проект | 200 OK |
| DELETE | `/api/v1/projects/{id}` | Удалить проект | 204 No Content |

### Задачи

Все endpoints задач требуют JWT-аутентификации.

| Метод | Endpoint | Описание | Успешный ответ |
|---|---|---|---|
| POST | `/api/v1/projects/{projectId}/tasks` | Создать задачу в проекте | 201 Created |
| GET | `/api/v1/projects/{projectId}/tasks` | Получить задачи проекта | 200 OK |
| GET | `/api/v1/tasks/{id}` | Получить задачу по ID | 200 OK |
| PUT | `/api/v1/tasks/{id}` | Обновить задачу | 200 OK |
| DELETE | `/api/v1/tasks/{id}` | Удалить задачу | 204 No Content |

### Проверка доступности

| Метод | Endpoint | Описание | Успешный ответ |
|---|---|---|---|
| GET | `/api/v1/health` | Проверить доступность API | 200 OK |

## Пагинация и сортировка

Endpoints, возвращающие списки, поддерживают query-параметры:

| Параметр | Значение по умолчанию | Описание |
|---|---:|---|
| `page` | 0 | Индекс страницы, начиная с нуля |
| `size` | 20 | Размер страницы, максимум 100 записей |
| `sort` | Зависит от endpoint | Поле и направление сортировки |

Пример запроса:

```http
GET /api/v1/projects?page=0&size=20&sort=createdAt,desc
```

Если запросить более 100 записей на странице, размер будет ограничен 100. Если указать несуществующее поле сортировки, API вернёт `400 Bad Request` с кодом ошибки `INVALID_SORT_PROPERTY`.

## Формат ошибок

Ошибки возвращаются в едином формате JSON:

```json
{
  "status": 404,
  "error": "PROJECT_NOT_FOUND",
  "message": "Project not found",
  "timestamp": "2026-10-26T12:00:00Z"
}
```

Основные коды ошибок:

| HTTP-статус | Код ошибки | Значение |
|---|---|---|
| 400 Bad Request | `VALIDATION_ERROR` | Ошибка валидации запроса |
| 400 Bad Request | `INVALID_SORT_PROPERTY` | Указано неизвестное поле сортировки |
| 401 Unauthorized | `UNAUTHORIZED` | Отсутствует авторизация или JWT недействителен |
| 401 Unauthorized | `INVALID_CREDENTIALS` | Неверный email или пароль |
| 404 Not Found | `PROJECT_NOT_FOUND` | Проект не найден или недоступен пользователю |
| 404 Not Found | `TASK_NOT_FOUND` | Задача не найдена |
| 409 Conflict | `USER_ALREADY_EXISTS` | Пользователь с таким email уже существует |

Каждый пользователь может работать только со своими проектами и задачами. Попытки доступа к ресурсам другого пользователя обрабатываются как `404 Not Found`.

## Архитектура

```text
HTTP / REST
     |
     v
API-контроллеры и DTO
     |
     v
Сервисы приложения
     |
     v
Репозитории
     |
     v
JPA / Hibernate
     |
     v
PostgreSQL
```

Проект разделяет обработку API-запросов, бизнес-логику, доменные модели и слой хранения данных. DTO используются на границе API, а Flyway управляет изменениями схемы базы данных.

## Миграции базы данных

SQL-миграции Flyway расположены в каталоге:

```text
src/main/resources/db/migration
```

Hibernate проверяет соответствие схемы базы данных сущностям, но не создаёт и не изменяет её автоматически.

## Непрерывная интеграция

GitHub Actions запускает Maven-проверки и сборку в соответствии с настройками workflow.

Конфигурация CI находится здесь:

```text
.github/workflows/ci.yml
```

## Статус проекта

Основной API проектов и задач реализован. Также реализованы JWT-аутентификация, проверка владельца ресурсов, пагинация, структурированные ошибки и Swagger-документация.

Unit- и интеграционные тесты покрывают основные сценарии работы приложения и проверки безопасности.