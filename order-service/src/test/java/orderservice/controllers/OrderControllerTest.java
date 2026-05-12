package orderservice.controllers;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.UUID;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import orderservice.models.dto.OrderUpdateRequest;
import orderservice.services.OrderService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

  private static final UUID USER_ID = randomUUID();
  private static final UUID ORDER_ID = randomUUID();

  @Mock private OrderService orderService;

  @InjectMocks private OrderController orderController;

  @Test
  void whenCreateOrderCalled_thenOrderResponseReturned() {
    OrderRequestDto request = Instancio.of(OrderRequestDto.class).create();
    OrderResponse orderResponse = Instancio.of(OrderResponse.class).create();

    when(orderService.createOrder(request, USER_ID)).thenReturn(orderResponse);

    ResponseEntity<OrderResponse> response = orderController.createOrder(request, USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isInstanceOf(OrderResponse.class).isEqualTo(orderResponse);
  }

  @Test
  void whenGetOrderCalled_thenOrderResponseReturned() {
    OrderResponse orderResponse = Instancio.of(OrderResponse.class).create();

    when(orderService.getOrderById(ORDER_ID, USER_ID)).thenReturn(orderResponse);

    ResponseEntity<OrderResponse> response = orderController.getOrder(ORDER_ID, USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isInstanceOf(OrderResponse.class).isEqualTo(orderResponse);
  }

  @Test
  void whenGetAllOrdersForUserCalled_thenOrderResponsesReturned() {
    List<OrderResponse> orders = Instancio.ofList(OrderResponse.class).size(3).create();

    when(orderService.getOrdersForUser(USER_ID)).thenReturn(orders);

    ResponseEntity<List<OrderResponse>> response = orderController.getAllOrdersForUser(USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(orders);
  }

  @Test
  void whenUpdateOrderStatusCalled_thenNoResponseBodyReturned() {
    OrderUpdateRequest request = Instancio.of(OrderUpdateRequest.class).create();

    doNothing().when(orderService).updateOrderStatus(ORDER_ID, request);

    ResponseEntity<OrderResponse> response = orderController.updateOrderStatus(ORDER_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }
}
