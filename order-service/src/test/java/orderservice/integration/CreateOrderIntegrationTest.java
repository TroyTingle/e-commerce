package orderservice.integration;

import static java.util.UUID.randomUUID;
import static orderservice.enums.OrderStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import orderservice.models.Order;
import orderservice.models.dto.OrderItemRequest;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Tag("integration")
class CreateOrderIntegrationTest extends BaseIntegrationTest {

  @Test
  @Transactional
  void createNewOrder_whenProductsExist_thenReturnOrderResponseAndPersistItems() {
    UUID keyboardId = randomUUID();
    UUID mouseId = randomUUID();
    OrderRequestDto request =
        new OrderRequestDto(
            List.of(new OrderItemRequest(keyboardId, 2), new OrderItemRequest(mouseId, 3)));

    when(productServiceClient.getProductByUuid(keyboardId))
        .thenReturn(buildProduct(keyboardId, "Keyboard", BigDecimal.valueOf(10.99)));
    when(productServiceClient.getProductByUuid(mouseId))
        .thenReturn(buildProduct(mouseId, "Mouse", BigDecimal.valueOf(5.25)));

    ResponseEntity<OrderResponse> response =
        orderServiceClient
            .post()
            .uri(ORDER_URL)
            .contentType(APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(OrderResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUserId()).isEqualTo(userId);
    assertThat(response.getBody().getOrderStatus()).isEqualTo(CREATED);
    assertThat(response.getBody().getTotalPrice()).isEqualByComparingTo("37.73");
    assertThat(response.getBody().getItems())
        .extracting("productName", "quantity")
        .containsExactlyInAnyOrder(tuple("Keyboard", 2), tuple("Mouse", 3));

    Order persistedOrder = orderRepository.findById(response.getBody().getOrderId()).orElseThrow();
    assertThat(persistedOrder.getItems())
        .hasSize(2)
        .allSatisfy(item -> assertThat(item.getOrder().getId()).isEqualTo(persistedOrder.getId()));
  }

  @Test
  void createNewOrder_whenItemsAreEmpty_thenReturnBadRequest() {
    OrderRequestDto request = new OrderRequestDto(List.of());

    assertThatThrownBy(
            () ->
                orderServiceClient
                    .post()
                    .uri(ORDER_URL)
                    .contentType(APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.BadRequest.class)
        .message()
        .contains("items");

    verify(productServiceClient, never()).getProductByUuid(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createNewOrder_whenUnauthenticated_thenReturnUnauthorized() {
    OrderRequestDto request = new OrderRequestDto(List.of(new OrderItemRequest(randomUUID(), 1)));

    assertThatThrownBy(
            () ->
                unauthenticatedOrderServiceClient
                    .post()
                    .uri(ORDER_URL)
                    .contentType(APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }
}
