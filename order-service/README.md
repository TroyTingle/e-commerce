# order-service
Spring Boot service for authenticated customer order creation, order lookup, and order status updates.

## Purpose
`order-service` owns customer order history. During order creation it calls `product-service` over gRPC, snapshots product details, calculates totals, and persists the order.

Order creation should fail if product-service lookup fails. Inventory reservation is future work and should happen asynchronously through Kafka-backed events once `inventory-service` exists.

## Responsibilities
- Accept customer order creation requests.
- Extract authenticated user UUID from the JWT principal.
- Resolve product details from `product-service` over gRPC.
- Snapshot product name and price at purchase time.
- Calculate order totals.
- Persist orders and embedded order items.
- Let admins update order status.

## Key Classes
- `OrderServiceApplication`: Spring Boot entry point.
- `OrderController`: REST order endpoints using generated OpenAPI DTOs.
- `OrderService`: order business logic.
- `OrderMapper`: request/entity/response mapping and product lookup during order creation.
- `ProductServiceClient`: gRPC client wrapper.
- `GrpcClientConfig`: managed channel and blocking stub setup.
- `SecurityConfig`: stateless JWT security configuration.
- `Order`, `OrderItem`: JPA order model.
- `OrderStatus`: order state enum.
- `OrderServiceControllerAdvice`: exception-to-HTTP mapping.

## REST API
Contract source: [`specs/api/order-service-api.yaml`](./specs/api/order-service-api.yaml).

Base path: `/api/v1/orders`. The Maven build generates `OrdersApiV1` and DTOs under `uk.co.ttingle.orderservice.generated.rest.v1`, with `OrderStatus` mapped to the domain enum.

All endpoints require a bearer token except actuator `health` and `info`.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/orders` | Create an order for the authenticated customer. |
| `GET` | `/api/v1/orders/{orderId}` | Get one order if it belongs to the authenticated customer. |
| `GET` | `/api/v1/orders` | List all orders for the authenticated customer. |
| `PATCH` | `/api/v1/orders/{orderId}` | Admin-only order status update. Intended response is `204 No Content`. |

`OrderRequestDto`:
```json
{
  "items": [
    {
      "productId": "00000000-0000-0000-0000-000000000000",
      "quantity": 2
    }
  ]
}
```

`OrderUpdateRequest`:
```json
{
  "newStatus": "PAID"
}
```

Current `OrderResponse`:
```json
{
  "orderId": "00000000-0000-0000-0000-000000000000",
  "userId": "00000000-0000-0000-0000-000000000000",
  "orderStatus": "CREATED",
  "totalPrice": 39.98,
  "items": [
    {
      "productName": "T-Shirt",
      "quantity": 2
    }
  ],
  "createdAt": "2026-06-12T12:00:00Z"
}
```

## Order Status
Current enum values:
- `CREATED`
- `PAYMENT_PENDING`
- `PAID`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

Suggested transition model:
- `CREATED -> PAYMENT_PENDING`
- `PAYMENT_PENDING -> PAID`
- `PAID -> SHIPPED`
- `SHIPPED -> DELIVERED`
- `CREATED`, `PAYMENT_PENDING`, or `PAID -> CANCELLED`
- Do not transition out of `DELIVERED`.
- Usually do not transition out of `CANCELLED`, except for manual/admin remediation. <!-- TODO: verify business rule -->

Feedback: payment events should drive `PAYMENT_PENDING -> PAID`; inventory/fulfillment events should drive shipment states. Manual admin transitions are useful for a portfolio demo, but a real flow should enforce transition rules in `OrderService`.

## Response Shape Feedback
Current `OrderItemResponse` only exposes `productName` and `quantity`. For a stronger ecommerce API, include:
- `productId`
- `sku`
- `productName`
- `quantity`
- `unitPriceAtPurchase`
- `lineTotal`
- `currency`

This would make receipts, customer support, audit trails, and UI rendering easier without another product-service lookup.

## Data Model
`Order`:
- `id`: UUID.
- `userId`: authenticated customer UUID.
- `status`: `OrderStatus`.
- `totalAmount`: calculated sum of order items.
- `createdAt`, `updatedAt`.
- `items`: JPA `@ElementCollection` stored in `order_items`.

`OrderItem`:
- `productId`.
- `productNameAtPurchase`.
- `quantity`.
- `priceAtPurchase`.

## Configuration
Main configuration is in `src/main/resources/application.yml`.

| Property | Default | Notes |
|----------|---------|-------|
| `server.port` | `8080` | Docker Compose maps this to host `8083`. |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/orderdb` | Override with `DATABASE_URL`. |
| `spring.datasource.username` | `user` | Override with `DATABASE_USERNAME`. |
| `spring.datasource.password` | `password` | Override with `DATABASE_PASSWORD`. |
| `spring.jpa.hibernate.ddl-auto` | `update` | Replace with migrations later. |
| `security.jwt.secret` | `your_very_secret_jwt_secret_key` | Override with `JWT_SECRET`. |
| `security.jwt.expiration` | `3600000` | One hour. |
| `product-service.grpc.address` | `product-service:9090` | Override with `PRODUCT_SERVICE_GRPC_ADDRESS`. |

The test config uses `ddl-auto=create-drop`, a test JWT secret, and `product-service.grpc.address=localhost:9090`.

## How to Run & Test
- Prerequisites: Java 25, Maven, PostgreSQL, Docker Compose, `common-lib`, and running product-service gRPC.
- Preferred local path from the repo root:
  ```sh
  mvn -pl order-service -am clean package
  docker compose -f infra/local/docker-compose.yml up order-service postgres product-service
  ```
- Direct Maven run with product-service running locally:
  ```sh
  PRODUCT_SERVICE_GRPC_ADDRESS=localhost:9090 mvn -pl order-service spring-boot:run
  ```
- Unit tests:
  ```sh
  mvn -pl order-service test
  ```
- Integration tests:
  ```sh
  mvn -pl order-service test -DtestTags=integration
  ```
- Maven profiles: none are declared.

## Dependencies
- `common-lib`
- `product-service` compile dependency for generated gRPC classes
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
- Product-service is a runtime dependency for order creation.
- Order creation currently snapshots product data but does not reserve or decrement stock.
- `GET /api/v1/orders` is not paginated yet.
- Docker Compose does not currently pass `JWT_SECRET` to this service, so local Compose runs use the development default from `application.yml`.

## TODO / Future Work
- Add status transition validation.
- Add gRPC failure mapping to clean HTTP errors.
- Add Kafka event publishing for inventory reservation.
- Add payment-driven order status changes.
- Expand order item responses with product ID, SKU, unit price, line total, and currency.
- Add pagination for order history.
