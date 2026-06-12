# gateway
Planned Spring Cloud Gateway edge service for public API routing, JWT validation, user-context forwarding, CORS, rate limiting, logging, and API documentation aggregation.

## Purpose
`gateway` is declared as a Maven module but currently has no checked-in application source, resources, or runtime entry point.

The intended role is to expose one public API namespace and keep edge concerns out of individual services.

## Intended Responsibilities
- Route public `/api/v1/**` traffic to downstream services.
- Validate JWT bearer tokens at the edge.
- Forward user identity and roles to downstream services.
- Apply CORS policy.
- Apply rate limiting.
- Add request/response logging and tracing context.
- Aggregate or link service OpenAPI docs.
- Provide health/readiness behavior for the edge layer.

## Current State
- `pom.xml` exists.
- `src/main/java`, `src/main/resources`, and test folders exist but contain no source files.
- No Spring Cloud Gateway dependency exists yet.
- No route configuration exists.
- No JWT validation behavior exists yet.

## Proposed Routing
| Public Path | Downstream Service | Notes |
|-------------|--------------------|-------|
| `/api/v1/auth/**` | `user-service` | Login/register should be public. |
| `/api/v1/users/**` | `user-service` | Future user profile endpoints. |
| `/api/v1/products/**` | `product-service` | Customer catalog reads. |
| `/api/v1/categories/**` | `product-service` | Category reads and future admin category management. |
| `/api/v1/admin/products/**` | `product-service` | Admin-only product management. |
| `/api/v1/orders/**` | `order-service` | Customer order APIs. |

Route table is intended design, not current code.

## JWT Strategy
The gateway should validate JWTs and forward identity context. For this portfolio project, reusing `common-lib` JWT helpers is acceptable.

Suggested forwarded headers:
- `X-User-Id`
- `X-User-Email`
- `X-User-Roles`

Downstream services currently validate JWTs themselves. A future decision is whether to keep defense-in-depth validation in each service or trust the gateway in local/internal networks. <!-- TODO: verify final trust model -->

## How to Run & Test
- Prerequisites: Java 25 and Maven.
- Build:
  ```sh
  mvn -pl gateway clean package
  ```
- Run locally: no application class exists yet.
- Unit tests:
  ```sh
  mvn -pl gateway test
  ```
- Integration tests: none are present.
- Maven profiles: none are declared.
- Spring profiles/properties: none are present.

## TODO / Future Work
- Add Spring Cloud Gateway dependencies.
- Add application entry point and route configuration.
- Validate JWTs and forward user context.
- Add CORS, rate limiting, request logging, and tracing propagation.
- Add route-level authorization rules.
- Add OpenAPI aggregation or documentation links.
- Add gateway integration tests with stub downstream services.
