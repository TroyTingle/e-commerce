# common-lib
Shared library for cross-service helpers used across ShopSphere services.

## Purpose
`common-lib` is broader than a tiny utility package. It currently centralizes authentication primitives, and it is expected to support future shared contracts/helpers while the codebase matures.

## Responsibilities
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
- Validate JWT secret length at application startup.
