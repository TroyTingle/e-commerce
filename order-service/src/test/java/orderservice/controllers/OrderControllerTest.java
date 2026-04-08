package orderservice.controllers;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.UUID;
import orderservice.enums.OrderStatus;
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
  void whenCreateOrderCalled_thenCreatedOrderReturned() {
    OrderRequestDto request = Instancio.of(OrderRequestDto.class).create();
    OrderResponse orderResponse =
        Instancio.of(OrderResponse.class)
            .set(field(OrderResponse::getOrderStatus), OrderStatus.CREATED)
            .set(field(OrderResponse::getCreatedAt), now())
            .create();

    when(orderService.createOrder(request, USER_ID)).thenReturn(orderResponse);

    ResponseEntity<OrderResponse> response = orderController.createOrder(request, USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isEqualTo(orderResponse);
    verify(orderService).createOrder(request, USER_ID);
  }

  @Test
  void whenGetOrderCalled_thenOrderReturned() {
    OrderResponse orderResponse = Instancio.of(OrderResponse.class).create();

    when(orderService.getOrderById(ORDER_ID)).thenReturn(orderResponse);

    ResponseEntity<OrderResponse> response = orderController.getOrder(ORDER_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(orderResponse);
    verify(orderService).getOrderById(ORDER_ID);
  }

  @Test
  void whenGetAllOrdersForUserCalled_thenUserOrdersReturned() {
    List<OrderResponse> orderResponses = Instancio.ofList(OrderResponse.class).create();

    when(orderService.getOrdersForUser(USER_ID)).thenReturn(orderResponses);

    ResponseEntity<List<OrderResponse>> response = orderController.getAllOrdersForUser(USER_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(orderResponses);
    verify(orderService).getOrdersForUser(USER_ID);
  }

  @Test
  void whenUpdateOrderStatusCalled_thenNoContentReturned() {
    OrderUpdateRequest updateRequest =
        OrderUpdateRequest.builder().newStatus(OrderStatus.PAID).build();

    doNothing().when(orderService).updateOrderStatus(ORDER_ID, updateRequest);

    ResponseEntity<OrderResponse> response =
        orderController.updateOrderStatus(ORDER_ID, updateRequest);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
    assertThat(response.getBody()).isNull();
    verify(orderService).updateOrderStatus(ORDER_ID, updateRequest);
  }
}
