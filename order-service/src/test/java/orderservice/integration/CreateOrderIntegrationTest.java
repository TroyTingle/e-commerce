package orderservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.UUID;
import orderservice.integration.testdata.OrderData;
import orderservice.models.dto.OrderItemRequest;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag("integration")
class CreateOrderIntegrationTest extends BaseIntegrationTest {

  private static final String ORDERS_URL = "/api/v1/orders";

  @Autowired private OrderData orderData;

  @Test
  void createOrder_whenAuthenticatedAndProductsExist_thenReturnCreatedOrder() {
    UUID userId = UUID.randomUUID();
    UUID firstProductId = UUID.randomUUID();
    UUID secondProductId = UUID.randomUUID();

    OrderRequestDto request =
        Instancio.of(OrderRequestDto.class)
            .set(
                field(OrderRequestDto::getItems),
                List.of(
                    new OrderItemRequest(firstProductId, 2),
                    new OrderItemRequest(secondProductId, 1)))
            .create();

    when(productServiceStub.getProductByUuid(orderData.productRequest(firstProductId)))
        .thenReturn(orderData.productResponse(firstProductId, "Keyboard", "10.50"));
    when(productServiceStub.getProductByUuid(orderData.productRequest(secondProductId)))
        .thenReturn(orderData.productResponse(secondProductId, "Mouse", "5.25"));

    ResponseEntity<OrderResponse> response =
        orderServiceClient
            .post()
            .uri(ORDERS_URL)
            .headers(headers -> headers.addAll(authHeaders(userId)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(OrderResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody())
        .isNotNull()
        .extracting(
            OrderResponse::getUserId, OrderResponse::getOrderStatus, OrderResponse::getTotalPrice)
        .isEqualTo(
            List.of(
                userId, orderservice.enums.OrderStatus.CREATED, new java.math.BigDecimal("26.25")));
    assertThat(response.getBody().getItems())
        .extracting(item -> item.getProductName(), item -> item.getQuantity())
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("Keyboard", 2),
            org.assertj.core.groups.Tuple.tuple("Mouse", 1));
  }

  @Test
  void createOrder_thenCreatedOrderCanBeFetchedById() {
    UUID userId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    OrderRequestDto request =
        Instancio.of(OrderRequestDto.class)
            .set(field(OrderRequestDto::getItems), List.of(new OrderItemRequest(productId, 3)))
            .create();

    when(productServiceStub.getProductByUuid(orderData.productRequest(productId)))
        .thenReturn(orderData.productResponse(productId, "Headphones", "12.00"));

    OrderResponse createdOrder =
        orderServiceClient
            .post()
            .uri(ORDERS_URL)
            .headers(headers -> headers.addAll(authHeaders(userId)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(OrderResponse.class);

    OrderResponse fetchedOrder =
        orderServiceClient
            .get()
            .uri(ORDERS_URL + "/" + createdOrder.getOrderId())
            .headers(headers -> headers.addAll(authHeaders(userId)))
            .retrieve()
            .body(OrderResponse.class);

    assertThat(fetchedOrder)
        .isNotNull()
        .extracting(
            OrderResponse::getOrderId,
            OrderResponse::getUserId,
            OrderResponse::getOrderStatus,
            OrderResponse::getTotalPrice)
        .isEqualTo(
            List.of(
                createdOrder.getOrderId(),
                userId,
                orderservice.enums.OrderStatus.CREATED,
                new java.math.BigDecimal("36.00")));
    assertThat(fetchedOrder.getItems())
        .extracting(item -> item.getProductName(), item -> item.getQuantity())
        .containsExactly(org.assertj.core.groups.Tuple.tuple("Headphones", 3));
  }
}
