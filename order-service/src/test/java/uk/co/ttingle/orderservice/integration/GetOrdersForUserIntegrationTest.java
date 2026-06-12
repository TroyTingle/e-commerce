package uk.co.ttingle.orderservice.integration;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.co.ttingle.orderservice.enums.OrderStatus.CREATED;
import static uk.co.ttingle.orderservice.enums.OrderStatus.PAID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.co.ttingle.orderservice.generated.rest.v1.dto.OrderResponse;
import uk.co.ttingle.orderservice.models.Order;

@Tag("integration")
class GetOrdersForUserIntegrationTest extends BaseIntegrationTest {

  @Test
  void getOrdersForAuthenticatedUser_whenMultipleUsersHaveOrders_thenReturnOnlyCurrentUserOrders() {
    Order currentUserOrder = orderRepository.save(buildOrderForUser(userId, CREATED));
    orderRepository.save(buildOrderForUser(randomUUID(), PAID));

    ResponseEntity<OrderResponse[]> response =
        orderServiceClient.get().uri(ORDER_URL).retrieve().toEntity(OrderResponse[].class);

    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody()[0].getOrderId()).isEqualTo(currentUserOrder.getId());
    assertThat(response.getBody()[0].getUserId()).isEqualTo(userId);
  }

  @Test
  void getOrdersForAuthenticatedUser_whenUserHasNoOrders_thenReturnEmptyList() {
    orderRepository.save(buildOrderForUser(randomUUID(), PAID));

    ResponseEntity<OrderResponse[]> response =
        orderServiceClient.get().uri(ORDER_URL).retrieve().toEntity(OrderResponse[].class);

    assertThat(response.getBody()).isEmpty();
  }

  @Test
  void getOrdersForAuthenticatedUser_whenUnauthenticated_thenReturnUnauthorized() {
    assertThatThrownBy(
            () ->
                unauthenticatedOrderServiceClient
                    .get()
                    .uri(ORDER_URL)
                    .retrieve()
                    .toEntity(OrderResponse[].class))
        .isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }
}
