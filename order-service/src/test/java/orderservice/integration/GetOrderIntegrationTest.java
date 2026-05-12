package orderservice.integration;

import static java.util.UUID.randomUUID;
import static orderservice.enums.OrderStatus.PAID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import orderservice.models.Order;
import orderservice.models.dto.OrderResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

@Tag("integration")
class GetOrderIntegrationTest extends BaseIntegrationTest {

  @Test
  void getOrder_whenOrderExistsAndBelongsToAuthenticatedUser_thenReturnOrderResponse() {
    Order order = orderRepository.save(buildOrderForUser(userId, PAID));

    ResponseEntity<OrderResponse> response =
        orderServiceClient
            .get()
            .uri(ORDER_URL + "/{orderId}", order.getId())
            .retrieve()
            .toEntity(OrderResponse.class);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getOrderId()).isEqualTo(order.getId());
    assertThat(response.getBody().getUserId()).isEqualTo(userId);
    assertThat(response.getBody().getOrderStatus()).isEqualTo(PAID);
    assertThat(response.getBody().getTotalPrice()).isEqualByComparingTo("19.99");
  }

  @Test
  void getOrder_whenOrderDoesNotExist_thenReturnNotFound() {
    UUID orderId = randomUUID();

    assertThatThrownBy(
            () ->
                orderServiceClient
                    .get()
                    .uri(ORDER_URL + "/{orderId}", orderId)
                    .retrieve()
                    .toEntity(OrderResponse.class))
        .isInstanceOf(HttpClientErrorException.NotFound.class)
        .message()
        .contains(orderId.toString());
  }

  @Test
  void getOrder_whenOrderBelongsToDifferentUser_thenReturnForbidden() {
    Order order = orderRepository.save(buildOrderForUser(randomUUID(), PAID));

    assertThatThrownBy(
            () ->
                orderServiceClient
                    .get()
                    .uri(ORDER_URL + "/{orderId}", order.getId())
                    .retrieve()
                    .toEntity(OrderResponse.class))
        .isInstanceOf(HttpClientErrorException.Forbidden.class)
        .message()
        .contains("authenticated user");
  }

  @Test
  void getOrder_whenUnauthenticated_thenReturnUnauthorized() {
    Order order = orderRepository.save(buildOrderForUser(userId, PAID));

    assertThatThrownBy(
            () ->
                unauthenticatedOrderServiceClient
                    .get()
                    .uri(ORDER_URL + "/{orderId}", order.getId())
                    .retrieve()
                    .toEntity(OrderResponse.class))
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }
}
