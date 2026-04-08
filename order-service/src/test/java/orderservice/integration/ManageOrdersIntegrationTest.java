package orderservice.integration;

import static orderservice.enums.OrderStatus.CREATED;
import static orderservice.enums.OrderStatus.PAID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.List;
import java.util.UUID;
import orderservice.integration.testdata.OrderData;
import orderservice.models.Order;
import orderservice.models.dto.OrderResponse;
import orderservice.models.dto.OrderUpdateRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag("integration")
class ManageOrdersIntegrationTest extends BaseIntegrationTest {

  private static final String ORDERS_URL = "/api/v1/orders";

  @Autowired private OrderData orderData;

  @Test
  void getAllOrdersForUser_whenUserHasOrders_thenReturnOnlyThatUsersOrders() {
    UUID matchingUserId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    Order firstOrder = orderData.createOrder(matchingUserId, "Keyboard", 2, "9.99", CREATED);
    Order secondOrder = orderData.createOrder(matchingUserId, "Mouse", 1, "5.25", PAID);
    orderData.createOrder(otherUserId, "Monitor", 1, "199.99", CREATED);

    List<OrderResponse> response =
        orderServiceClient
            .get()
            .uri(ORDERS_URL)
            .headers(headers -> headers.addAll(authHeaders(matchingUserId)))
            .retrieve()
            .body(
                new org.springframework.core.ParameterizedTypeReference<List<OrderResponse>>() {});

    assertThat(response)
        .hasSize(2)
        .extracting(OrderResponse::getOrderId)
        .containsExactlyInAnyOrder(firstOrder.getId(), secondOrder.getId());
    assertThat(response).extracting(OrderResponse::getUserId).containsOnly(matchingUserId);
  }

  @Test
  void updateOrderStatus_whenAuthenticated_thenStatusIsPersisted() {
    UUID userId = UUID.randomUUID();
    Order order = orderData.createOrder(userId, "Keyboard", 2, "9.99", CREATED);
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(PAID).build();

    ResponseEntity<Void> response =
        orderServiceClient
            .patch()
            .uri(ORDERS_URL + "/" + order.getId())
            .headers(headers -> headers.addAll(authHeaders(userId)))
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toBodilessEntity();

    OrderResponse updatedOrder =
        orderServiceClient
            .get()
            .uri(ORDERS_URL + "/" + order.getId())
            .headers(headers -> headers.addAll(authHeaders(userId)))
            .retrieve()
            .body(OrderResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
    assertThat(updatedOrder).isNotNull();
    assertThat(updatedOrder.getOrderStatus()).isEqualTo(PAID);
  }
}
