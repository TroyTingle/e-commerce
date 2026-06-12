# common-lib
Shared library for cross-service DTOs, API error models, and JWT security helpers used across ShopSphere services.

## Purpose
`common-lib` is broader than a tiny utility package. It currently centralizes shared DTOs and authentication primitives, and it is expected to support future shared contracts/helpers while the codebase matures.

REST request/response DTOs have started moving into generated service-local contracts under `specs/api/*.yaml`. `common-lib` still contains `ProductDto`, which is currently used by the order-service gRPC client path.

## Responsibilities
- Provide common DTOs:
  - `ExceptionDto`
  - `ProductDto`
- Provide JWT constants and helpers:
  - `JwtConstants`
  - `JwtTokenUtil`
  - `JwtAuthenticationFilter`
- Allow services to parse bearer tokens into a Spring Security authentication principal.

## JWT Contract
`JwtTokenUtil.generateUserToken(UUID userId, String email, Collection<String> roles)` creates tokens with:
- Subject: user UUID as a string.
- Claim `email`: user email address.
- Claim `roles`: list of role strings such as `CUSTOMER` or `ADMIN`.
- Expiration: derived from `security.jwt.expiration` in milliseconds.
- Signature secret: derived from `security.jwt.secret`.

`JwtAuthenticationFilter` reads `Authorization: Bearer <token>`, validates the token, extracts the user UUID as the authentication principal, and converts token roles into `SimpleGrantedAuthority` values.

## Gateway Usage Note
The gateway is planned to validate JWTs and forward user context. Reusing `JwtTokenUtil` in the gateway is reasonable for this portfolio project, but avoid making the gateway depend on broad domain DTOs from `common-lib`. A cleaner future split would be:
- `security-contracts` or `auth-common` for JWT helpers.
- `api-contracts` for generated/shared request and response contracts.
- Service-private DTOs inside each service.

## How to Test
- Prerequisites: Java 25 and Maven.
- Run unit tests:
  ```sh
  mvn -pl common-lib test
  ```
- Integration tests: none are present.
- Maven profiles: none are declared.
- Required consumer properties:
  - `security.jwt.secret`
  - `security.jwt.expiration`

## Dependencies
- Spring Context
- Spring Security Core/Web
- Jakarta Servlet API
- Jakarta Validation API
- JJWT
- Lombok

## TODO / Future Work
- Add unit tests for `JwtTokenUtil` token generation, parsing, expiration, and invalid-token handling.
- Add unit tests for `JwtAuthenticationFilter`.
- Split security helpers from shared DTOs before the gateway starts depending on the module heavily.
- Move or replace `ProductDto` with generated/internal service contracts once the gRPC/client contract is cleaned up.
- Validate JWT secret length at application startup.
- Define a consistent cross-service error response shape around `ExceptionDto`.
