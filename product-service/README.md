# product-service
Spring Boot catalog service that owns products and categories, exposes REST APIs for catalog/admin workflows, and provides an internal gRPC product lookup for order creation.

## Purpose
`product-service` is the current source of truth for catalog data. It temporarily stores inventory quantity until `inventory-service` becomes the stock/reservation authority.

The service has both public/customer-facing reads and admin-facing writes. REST interfaces and DTOs are generated from `specs/api/product-service-api.yaml`. Admin authorization is intended but not fully implemented yet.

## Responsibilities
- Store products and categories in PostgreSQL.
- Expose product search and lookup APIs.
- Expose category lookup APIs.
- Expose admin product create/update/deactivate operations.
- Temporarily expose inventory quantity updates.
- Provide an internal-only gRPC API for `order-service`.

## Key Classes
- `ProductServiceApplication`: Spring Boot entry point.
- `ProductController`: customer product search and lookup endpoints implementing generated `ProductsApiV1`.
- `AdminProductController`: admin product mutation endpoints implementing generated `AdminProductsApiV1`.
- `CategoryController`: category endpoints implementing generated `CategoriesApiV1`.
- `ProductGrpcService`: internal gRPC product lookup.
- `ProductService`: product business logic and filtering.
- `CategoryService`: category read logic.
- `ProductSpecification`: dynamic product search filters.
- `ProductMapper`, `CategoryMapper`: entity/DTO mapping.
- `Product`, `Category`: JPA entities.
- `ProductServiceControllerAdvice`: exception-to-HTTP mapping.

## REST API
Contract source: `specs/api/product-service-api.yaml`.

The Maven build generates Spring interfaces under `uk.co.ttingle.productservice.generated.rest.v1` and DTOs under `uk.co.ttingle.productservice.generated.rest.v1.dto`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/products` | Search products with filters and Spring pageable query params. |
| `GET` | `/api/v1/products/{productId}` | Get a product by UUID. |
| `GET` | `/api/v1/products/sku/{sku}` | Get a product by SKU. |
| `GET` | `/api/v1/categories` | List categories. |
| `GET` | `/api/v1/categories/name?category={name}` | Get a category by name according to the OpenAPI spec. The controller currently uses `@PathVariable`, so the spec/controller annotations need aligning. |
| `POST` | `/api/v1/admin/products` | Create a product. Should require admin authority. |
| `PUT` | `/api/v1/admin/products/{productId}` | Update a product. Should require admin authority. |
| `DELETE` | `/api/v1/admin/products/{productId}` | Soft-deactivate a product. Should require admin authority. |
| `PATCH` | `/api/v1/admin/products/id/inventory` | Temporarily set product inventory quantity until `inventory-service` exists. The spec likely should use `{productId}` here. |

Search query parameters:
- `category`
- `minPrice`
- `maxPrice`
- `search`
- `active`, default `true`
- Spring pageable parameters such as `page`, `size`, and `sort`

`ProductRequest`:
```json
{
  "name": "T-Shirt",
  "description": "Cotton tee",
  "price": 19.99,
  "currency": "GBP",
  "sku": "TSHIRT-001",
  "inventoryQuantity": 10,
  "categoryName": "Clothing"
}
```

Currency intent: store a 3-character currency code. Intended supported values are `GBP` and `USD`; user note said `GPD`, assumed to mean `GBP`. <!-- TODO: verify currency code -->

## gRPC API
Defined in `specs/proto/product.proto`.

`ProductGrpcService` implements:
- `ecommerce.product.v1.ProductService/GetProductByUuid`

Request:
```proto
message ProductRequest {
  string product_id = 1;
}
```

Response:
```proto
message ProductResponse {
  string id = 1;
  string name = 2;
  string description = 3;
  int64 price = 4;
  string sku = 5;
  string category = 6;
  string currency = 7;
}
```

`price` is returned in minor units. The Java implementation converts from `BigDecimal` by multiplying by 100 and rounding half-up.

This API is intended to be internal-only for service-to-service communication.

## Data Model
`Product`:
- `id`: UUID.
- `name`.
- `description`: up to 2000 chars.
- `price`: decimal precision 10, scale 2.
- `currency`: 3-char string.
- `sku`: unique.
- `inventoryQuantity`: temporary stock field.
- `active`: used for soft deletion/filtering.
- `category`: many-to-one category relation.
- `createdAt`, `updatedAt`.

`Category`:
- `id`: UUID.
- `name`: unique.
- `description`.
- `createdAt`, `updatedAt`.

## Configuration
Main configuration is in `src/main/resources/application.yml`.

| Property | Default | Notes |
|----------|---------|-------|
| `server.port` | `8080` | Docker Compose maps this to host `8082`. |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/productdb` | Override with `DATABASE_URL`. |
| `spring.datasource.username` | `user` | Override with `DATABASE_USERNAME`. |
| `spring.datasource.password` | `password` | Override with `DATABASE_PASSWORD`. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Replace with migrations later. |
| `spring.grpc.server.port` | `9090` | Internal gRPC API. |
| `security.jwt.secret` | `your_very_secret_jwt_secret_key` | Override with `JWT_SECRET`. |
| `security.jwt.expiration` | `3600000` | One hour. |

The test profile uses `ddl-auto=create-drop` and disables the gRPC server.

## How to Run & Test
- Prerequisites: Java 25, Maven, PostgreSQL, Docker Compose, and `common-lib`.
- Preferred local path from the repo root:
  ```sh
  mvn -pl product-service -am clean package
  docker compose -f infra/local/docker-compose.yml up product-service postgres
  ```
- Direct Maven run:
  ```sh
  mvn -pl product-service spring-boot:run
  ```
- Unit tests:
  ```sh
  mvn -pl product-service test
  ```
- Integration tests:
  ```sh
  mvn -pl product-service test -DtestTags=integration
  ```
- Maven profiles: none are declared.

## Dependencies
- `common-lib`
- Spring Boot Web MVC
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring gRPC
- Spring Actuator
- Spring OpenTelemetry starter
- PostgreSQL JDBC
- Testcontainers PostgreSQL

## Operational Notes
- Admin endpoints should require admin authority, but the code currently only has a TODO comment.
- There is no product-service-specific `SecurityConfig` yet.
- Product controllers currently have class-level `@RequestMapping` values while generated interfaces also use absolute `/api/v1/...` paths; verify effective Spring mappings to avoid duplicated prefixes.
- The OpenAPI spec has two path issues to fix: literal `/api/v1/admin/products/id/inventory` and `/api/v1/categories/name` with a query parameter while the controller uses `@PathVariable`.
- `inventoryQuantity` is temporary and should move to `inventory-service`.
- Category admin CRUD does not exist yet but is intended.
- Product update does not currently check for duplicate SKU conflicts.

## TODO / Future Work
- Add admin authorization to `/api/v1/admin/products/**`.
- Add service security configuration or intentionally document why the service relies on gateway-only auth.
- Align generated OpenAPI paths and controller annotations for category lookup and inventory update.
- Remove duplicate request mapping prefixes or regenerate interfaces with relative method paths.
- Add category create/update/delete endpoints.
- Validate currency against supported values: `GBP` and `USD`.
- Move inventory state out to `inventory-service`.
- Add optimistic locking or event-based inventory consistency before real stock flows.
- Add gRPC integration tests.
- Add seed data or migration scripts for categories.
