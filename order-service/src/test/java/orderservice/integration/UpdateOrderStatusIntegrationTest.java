package orderservice.integration;

import static orderservice.enums.OrderStatus.CANCELLED;
import static orderservice.enums.OrderStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import orderservice.models.Order;
import orderservice.models.dto.OrderUpdateRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

@Tag("integration")
class UpdateOrderStatusIntegrationTest extends BaseIntegrationTest {

  @Test
  void updateOrderStatus_whenAdminAndOrderExists_thenReturnNoContentAndPersistStatus() {
    Order order = orderRepository.save(buildOrderForUser(userId, CREATED));
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(CANCELLED).build();

    ResponseEntity<Void> response =
        adminOrderServiceClient
            .patch()
            .uri(ORDER_URL + "/{orderId}", order.getId())
            .contentType(APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toBodilessEntity();

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
    assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
        .isEqualTo(CANCELLED);
  }

  @Test
  void updateOrderStatus_whenStatusIsNull_thenReturnBadRequest() {
    Order order = orderRepository.save(buildOrderForUser(userId, CREATED));
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(null).build();

    assertThatThrownBy(
            () ->
                adminOrderServiceClient
                    .patch()
                    .uri(ORDER_URL + "/{orderId}", order.getId())
                    .contentType(APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.BadRequest.class)
        .message()
        .contains("newStatus");
  }

  @Test
  void updateOrderStatus_whenAuthenticatedUserIsNotAdmin_thenReturnForbidden() {
    Order order = orderRepository.save(buildOrderForUser(userId, CREATED));
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(CANCELLED).build();

    assertThatThrownBy(
            () ->
                orderServiceClient
                    .patch()
                    .uri(ORDER_URL + "/{orderId}", order.getId())
                    .contentType(APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Forbidden.class);
  }

  @Test
  void updateOrderStatus_whenUnauthenticated_thenReturnUnauthorized() {
    Order order = orderRepository.save(buildOrderForUser(userId, CREATED));
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(CANCELLED).build();

    assertThatThrownBy(
            () ->
                unauthenticatedOrderServiceClient
                    .patch()
                    .uri(ORDER_URL + "/{orderId}", order.getId())
                    .contentType(APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity())
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }
}
