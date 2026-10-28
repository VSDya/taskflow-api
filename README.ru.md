# TaskFlow API

REST API для управления проектами и задачами на Java 21 и Spring Boot 3. Проект демонстрирует JWT-аутентификацию, проверку владельца ресурсов, хранение данных в PostgreSQL, миграции Flyway, документацию API, автоматизированные тесты, Docker Compose и CI.

**English version:** [README.md](README.md)

> **Портфолио-проект.** Репозиторий демонстрирует практики backend-разработки и тестирования; он не заявляется как готовое к production решение.

## Содержание

- [Возможности](#возможности)
- [Технологический стек](#технологический-стек)
- [Требования](#требования)
- [Запуск](#запуск-проекта)
- [API](#api-endpoints)
- [Пагинация и сортировка](#пагинация-и-сортировка)
- [Формат ошибок](#формат-ошибок)
- [Архитектура и миграции](#архитектура-и-миграции)
- [Тестирование и CI](#тестирование-и-ci)

## Возможности

- Регистрация пользователей и вход с выдачей подписанного JWT
- Хеширование паролей с помощью BCrypt
- CRUD-операции для проектов и задач
- Проверка владельца ресурсов: пользователь может работать только со своими проектами и задачами
- Статусы задач: `TODO`, `IN_PROGRESS`, `DONE`
- Приоритеты задач: `LOW`, `MEDIUM`, `HIGH`
- Валидация входящих запросов и единый формат JSON-ошибок
- Пагинация и сортировка списков
- Миграции схемы базы данных через Flyway
- Unit-, MVC- и интеграционные тесты с PostgreSQL
- Документация OpenAPI и Swagger UI
- Docker Compose и GitHub Actions CI

## Технологический стек

- Java 21, Spring Boot 3
- Spring Web, Spring Validation, Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL 16, Flyway
- JJWT, BCrypt
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito, MockMvc, Testcontainers
- Maven, Docker Compose, GitHub Actions

## Требования

- JDK 21
- Maven 3.9 или новее
- Docker Desktop или Docker Engine с поддержкой Docker Compose
- OpenSSL (необязательно, для генерации JWT-секрета)
- Git

## Запуск проекта

### Вариант 1. Запуск через Docker Compose

Создай локальный файл конфигурации из шаблона:

```powershell
Copy-Item .env.example .env
```

Открой `.env` и замени значения-заполнители:

- `POSTGRES_PASSWORD` — надёжный пароль PostgreSQL.
- `JWT_SECRET` — случайный секрет для подписи JWT. Сгенерировать его можно командой:

```powershell
openssl rand -base64 48
```

Скопируй результат в `JWT_SECRET` в файле `.env`. Приложение не использует секрет по умолчанию и не запустится, если `JWT_SECRET` отсутствует или слишком короткий для настроенного HMAC-алгоритма. **Не добавляй `.env` в Git и не публикуй его содержимое.**

Запусти API и PostgreSQL:

```powershell
docker compose up --build -d
```

Полезные адреса:

- API: [http://localhost:8080](http://localhost:8080)
- Health check: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health) — публичный endpoint, токен не нужен
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Посмотреть логи приложения:

```powershell
docker compose logs -f app
```

Остановить контейнеры, сохранив данные PostgreSQL:

```powershell
docker compose down
```

**Важно:** данные PostgreSQL сохраняются в Docker volume. Команда `docker compose down -v` удаляет volume с локальной базой данных и находящиеся в ней данные. Используй её только в том случае, если действительно хочешь удалить эти данные.

### Вариант 2. Запуск через Maven

Этот вариант предназначен для локальной разработки. Сначала создай файл `.env` по инструкции выше и запусти PostgreSQL:

```powershell
docker compose up -d postgres
```

Перед запуском приложения задай в текущей оболочке переменные окружения `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` и `JWT_SECRET`. Убедись, что реквизиты подключения совпадают с настройками запущенной базы данных. Затем выполни:

```powershell
mvn spring-boot:run
```

## API endpoints

Все маршруты API используют префикс `/api/v1`.

### Аутентификация

| Метод | Endpoint | Описание | Успешный ответ |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Регистрация пользователя | `201 Created` |
| POST | `/api/v1/auth/login` | Вход и получение JWT | `200 OK` |

Пример тела запроса для регистрации или входа:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123"
}
```

Пароль должен содержать от 8 до 72 символов. Регистрация создаёт учётную запись, но не возвращает JWT — для получения токена выполни вход. Стандартный срок действия токена — один час.

### Проекты

Все endpoints проектов требуют JWT-аутентификации.

| Метод | Endpoint | Описание | Успешный ответ |
| --- | --- | --- | --- |
| POST | `/api/v1/projects` | Создать проект | `201 Created` |
| GET | `/api/v1/projects` | Получить список своих проектов | `200 OK` |
| GET | `/api/v1/projects/{id}` | Получить проект по ID | `200 OK` |
| PUT | `/api/v1/projects/{id}` | Обновить проект | `200 OK` |
| DELETE | `/api/v1/projects/{id}` | Удалить проект | `204 No Content` |

### Задачи

Все endpoints задач требуют JWT-аутентификации.

| Метод | Endpoint | Описание | Успешный ответ |
| --- | --- | --- | --- |
| POST | `/api/v1/projects/{projectId}/tasks` | Создать задачу в проекте | `201 Created` |
| GET | `/api/v1/projects/{projectId}/tasks` | Получить задачи проекта | `200 OK` |
| GET | `/api/v1/tasks/{id}` | Получить задачу по ID | `200 OK` |
| PUT | `/api/v1/tasks/{id}` | Обновить задачу | `200 OK` |
| DELETE | `/api/v1/tasks/{id}` | Удалить задачу | `204 No Content` |

Для защищённых endpoint передавай токен в заголовке:

```http
Authorization: Bearer <JWT>
```

В Swagger UI нажми **Authorize** и вставь только сам токен, без префикса `Bearer ` — Swagger UI добавит его самостоятельно.

### Проверка доступности

| Метод | Endpoint | Описание | Успешный ответ |
| --- | --- | --- | --- |
| GET | `/api/v1/health` | Проверить доступность API | `200 OK` |

Health endpoint публичный и не требует JWT.

## Пагинация и сортировка

Endpoints, возвращающие списки, поддерживают query-параметры:

| Параметр | Значение по умолчанию | Описание |
| --- | --- | --- |
| `page` | `0` | Индекс страницы, начиная с нуля |
| `size` | `20` | Размер страницы, максимум 100 записей |
| `sort` | Зависит от endpoint | Поле и направление сортировки |

Пример:

```http
GET /api/v1/projects?page=0&size=20&sort=createdAt,desc
```

Для списков проектов и задач используется единая структура JSON-ответа с метаданными пагинации.

Пример ответа:

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

Это иллюстративный пример. `content` содержит элементы текущей страницы, а остальные поля описывают страницу и общее количество найденных элементов.

| Поле | Описание |
| --- | --- |
| `content` | Элементы текущей страницы |
| `page` | Индекс текущей страницы, начиная с нуля |
| `size` | Размер страницы, максимум 100 записей |
| `totalElements` | Общее количество найденных элементов |
| `totalPages` | Общее количество страниц |
| `first` | Признак первой страницы |
| `last` | Признак последней страницы |

Если указать неизвестное поле сортировки, API вернёт `400 Bad Request` с кодом ошибки `INVALID_SORT_PROPERTY`.

## Формат ошибок

Ошибки возвращаются в едином формате JSON. Пример:

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
| --- | --- | --- |
| `400 Bad Request` | `VALIDATION_ERROR` | Ошибка валидации запроса |
| `400 Bad Request` | `INVALID_SORT_PROPERTY` | Указано неизвестное поле сортировки |
| `401 Unauthorized` | `UNAUTHORIZED` | Отсутствует авторизация или JWT недействителен |
| `401 Unauthorized` | `INVALID_CREDENTIALS` | Неверный email или пароль |
| `404 Not Found` | `PROJECT_NOT_FOUND` | Проект не найден или недоступен пользователю |
| `404 Not Found` | `TASK_NOT_FOUND` | Задача не найдена |
| `409 Conflict` | `USER_ALREADY_EXISTS` | Пользователь с таким email уже существует |

Попытки доступа к проектам и задачам другого пользователя обрабатываются как `404 Not Found`.

## Архитектура и миграции

```text
HTTP / REST
     ↓
Контроллеры и валидация DTO
     ↓
Сервисы приложения и проверки владельца
     ↓
Репозитории
     ↓
JPA / Hibernate
     ↓
PostgreSQL (миграции Flyway)
```

SQL-миграции Flyway находятся в каталоге `src/main/resources/db/migration`. Flyway управляет изменениями схемы, а Hibernate проверяет соответствие схемы сущностям.

## Тестирование и CI

Запуск полного набора проверок:

```powershell
mvn -B verify
```

Интеграционные тесты используют Testcontainers и запускают собственные контейнеры PostgreSQL, поэтому отдельно установленный или вручную запущенный PostgreSQL не требуется. Docker Desktop / Docker Engine должен работать, чтобы Testcontainers мог создавать контейнеры.

GitHub Actions проверяет конфигурацию Docker Compose, запускает Maven verification и сборку. Workflow находится в файле `.github/workflows/ci.yml`.

## Поддержка production

Проект не заявляется как готовый к production. Перед реальным развёртыванием следует рассмотреть ограничение частоты запросов и защиту от злоупотреблений при аутентификации, централизованное хранение и ротацию секретов, метрики и наблюдаемость, health checks для конкретной среды развёртывания, а также документированную процедуру резервного копирования и восстановления.
