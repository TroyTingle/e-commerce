# gateway
Spring Cloud Gateway edge service for public API routing, JWT validation, user-context forwarding, CORS, rate limiting, and logging.

## Purpose
`gateway` exposes one public API namespace and keeps edge concerns out of individual services.

## Responsibilities
- Route public `/api/v1/**` traffic to downstream services.
- Validate JWT bearer tokens at the edge.
- Forward user identity and roles to downstream services.
- Apply CORS policy.
- Apply simple in-memory rate limiting.
- Add request/response logging and tracing context.
- Provide health/readiness behavior for the edge layer.

## Current State
- Spring Boot application entry point exists.
- Spring Cloud Gateway routes are configured in Java.
- JWT validation and `X-User-*` header forwarding are implemented as a reactive gateway filter.
- Basic request logging, CORS, actuator health/info/metrics/prometheus, and rate limiting are configured.

## Routing
| Public Path | Downstream Service | Notes |
|-------------|--------------------|-------|
| `/api/v1/auth/**` | `user-service` | Login/register should be public. |
| `/api/v1/users/**` | `user-service` | Requires a valid bearer token. |
| `GET /api/v1/products/**` | `product-service` | Public customer catalog reads. |
| `GET /api/v1/categories/**` | `product-service` | Public category reads. |
| `/api/v1/admin/products/**` | `product-service` | Requires a valid bearer token with `ADMIN` in the `roles` claim. |
| `/api/v1/orders/**` | `order-service` | Requires a valid bearer token. |

## JWT Strategy
The gateway validates JWTs with `common-lib` JWT helpers and forwards identity context.

Forwarded headers:
- `X-User-Id`
- `X-User-Email`
- `X-User-Roles`

Downstream services currently validate JWTs themselves. A future decision is whether to keep defense-in-depth validation in each service or trust the gateway in local/internal networks. <!-- TODO: verify final trust model -->

## Configuration
| Property | Default | Environment Variable |
|----------|---------|----------------------|
| `gateway.services.user-service-url` | `http://localhost:8081` | `USER_SERVICE_URL` |
| `gateway.services.product-service-url` | `http://localhost:8082` | `PRODUCT_SERVICE_URL` |
| `gateway.services.order-service-url` | `http://localhost:8083` | `ORDER_SERVICE_URL` |
| `gateway.rate-limit.requests` | `120` | `GATEWAY_RATE_LIMIT_REQUESTS` |
| `gateway.rate-limit.window` | `1m` | `GATEWAY_RATE_LIMIT_WINDOW` |
| `security.jwt.secret` | `your_very_secret_jwt_secret_key_32` | `JWT_SECRET` |

## How to Run & Test
- Prerequisites: Java 25 and Maven.
- Build:
  ```sh
  mvn -pl gateway -am clean package
  ```
- Run locally:
  ```sh
  mvn -pl gateway -am spring-boot:run
  ```
- Unit tests:
  ```sh
  mvn -pl gateway -am test
  ```
- Integration tests: none are present.

## TODO / Future Work
- Add gateway integration tests with stub downstream services.
- Replace in-memory rate limiting with a distributed limiter if multiple gateway instances are run.
- Decide whether downstream services keep validating JWTs or trust gateway-forwarded identity on internal networks.
