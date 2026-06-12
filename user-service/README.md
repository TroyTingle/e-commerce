# user-service
Spring Boot service for customer registration, login, password hashing, role storage, and JWT issuance.

## Purpose
`user-service` owns identity data for ShopSphere. It is currently focused on authentication, with user profile endpoints, seller signup, password reset, email verification, and refresh tokens planned for later.

This is a portfolio/learning service. REST interfaces and DTOs are generated from `specs/api/user-service-api.yaml`.

## Responsibilities
- Register new customer users.
- Hash passwords with BCrypt.
- Authenticate users by email/password.
- Issue JWT bearer tokens using `common-lib`.
- Persist users in PostgreSQL.
- Provide Spring Security `UserDetailsService` support.

## Key Classes
- `UserServiceApplication`: Spring Boot entry point.
- `UserAuthController`: login/register REST controller implementing generated `AuthenticationApiV1`.
- `UserAuthService`: registration and login business logic.
- `UserService`: loads users for Spring Security authentication.
- `SecurityConfig`: stateless security configuration, password encoder, and authentication manager.
- `UserAuthControllerAdvice`: maps auth exceptions to API error responses.
- `User`: JPA entity for `users`.
- `Role`: currently `CUSTOMER` and `ADMIN`.

## API
Contract source: `specs/api/user-service-api.yaml`.

The Maven build generates `uk.co.ttingle.userservice.generated.rest.v1.AuthenticationApiV1` and DTOs under `uk.co.ttingle.userservice.generated.rest.v1.dto`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/auth/login` | Authenticate and return a bearer token. Intended design is `POST`, but the current OpenAPI spec still defines `GET`. |
| `POST` | `/api/v1/auth/register` | Register a new customer user. |

`RegisterRequest`:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Ada",
  "lastName": "Lovelace"
}
```

`AuthResponse`:
```json
{
  "token": "<jwt>",
  "type": "Bearer"
}
```

## Data Model
`User` persists:
- `id`: generated UUID.
- `firstName`, `lastName`.
- `email`: unique.
- `password`: BCrypt hash.
- `role`: `CUSTOMER` or `ADMIN`.
- `createdAt`, `updatedAt`.

Current signup assigns every registered user `CUSTOMER`. Seller signup is not implemented yet.

## Configuration
Main configuration is in `src/main/resources/application.yml`.

| Property | Default | Notes |
|----------|---------|-------|
| `server.port` | `8080` | Docker Compose maps this to host `8081`. |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/userdb` | Override with `DATABASE_URL`. |
| `spring.datasource.username` | none | Required via `DATABASE_USERNAME`. |
| `spring.datasource.password` | none | Required via `DATABASE_PASSWORD`. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Suitable for learning/local use; migrations should replace this later. |
| `security.jwt.secret` | `your_very_secret_jwt_secret_key` | Override with `JWT_SECRET`. |
| `security.jwt.expiration` | `3600000` | One hour in milliseconds. |

Actuator exposes `health`, `info`, `metrics`, and `prometheus`.

## How to Run & Test
- Prerequisites: Java 25, Maven, PostgreSQL, Docker Compose, and `common-lib`.
- Preferred local path from the repo root:
  ```sh
  mvn -pl user-service -am clean package
  docker compose -f infra/local/docker-compose.yml up user-service postgres
  ```
- Direct Maven run:
  ```sh
  DATABASE_USERNAME=user DATABASE_PASSWORD=password mvn -pl user-service spring-boot:run
  ```
- Unit tests:
  ```sh
  mvn -pl user-service test
  ```
- Integration tests: no `@Tag("integration")` tests are present yet.
- Maven profiles: none are declared.

## Dependencies
- `common-lib`
- Spring Boot Web MVC
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Actuator
- Spring OpenTelemetry starter
- PostgreSQL JDBC
- Testcontainers dependencies are present but no integration tests are currently tagged.

## Operational Notes
- Login/register should be publicly accessible, but current security allowlist uses `/api/auth/**`, which does not match `/api/v1/auth/**`.
- Admin user creation is a future ticket.
- User profile endpoints are planned but not implemented.

## TODO / Future Work
- Change login from `GET /api/v1/auth/login` to `POST /api/v1/auth/login` in the OpenAPI spec and controller contract.
- Align security allowlist with `/api/v1/auth/**`.
- Add seller signup flow.
- Add admin user provisioning strategy.
- Add user profile endpoints.
- Add password reset, email verification, and refresh tokens.
- Add Testcontainers integration tests for registration and login.
- Remove default JWT secrets outside local development.
