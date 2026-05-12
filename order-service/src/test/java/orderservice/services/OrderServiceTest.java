package orderservice.services;

import static java.util.UUID.randomUUID;
import static orderservice.enums.OrderStatus.CREATED;
import static orderservice.enums.OrderStatus.PAID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import orderservice.exceptions.OrderNotFoundException;
import orderservice.mappers.OrderMapper;
import orderservice.models.Order;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import orderservice.models.dto.OrderUpdateRequest;
import orderservice.repositories.OrderRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  private static final UUID USER_ID = randomUUID();
  private static final UUID ORDER_ID = randomUUID();

  @Mock private OrderRepository orderRepository;
  @Mock private OrderMapper orderMapper;
  @InjectMocks private OrderService orderService;

  @Test
  void whenCreateOrderCalled_thenReturnCreatedOrderResponse() {
    OrderRequestDto request = Instancio.of(OrderRequestDto.class).create();
    Order mappedOrder = buildOrder();
    Order savedOrder = buildOrder();
    OrderResponse orderResponse = buildOrderResponse();

    when(orderMapper.toNewOrder(request, USER_ID)).thenReturn(mappedOrder);
    when(orderRepository.save(mappedOrder)).thenReturn(savedOrder);
    when(orderMapper.toOrderResponse(savedOrder)).thenReturn(orderResponse);

    OrderResponse response = orderService.createOrder(request, USER_ID);

    assertThat(response).isEqualTo(orderResponse);
    verify(orderMapper).toNewOrder(request, USER_ID);
    verify(orderRepository).save(mappedOrder);
    verify(orderMapper).toOrderResponse(savedOrder);
  }

  @Test
  void whenGetOrderByIdCalled_thenReturnMappedOrder() {
    Order order = buildOrder();
    OrderResponse orderResponse = buildOrderResponse();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

    OrderResponse response = orderService.getOrderById(ORDER_ID, USER_ID);

    assertThat(response).isEqualTo(orderResponse);
    verify(orderRepository).findById(ORDER_ID);
    verify(orderMapper).toOrderResponse(order);
  }

  @Test
  void whenGetOrderByIdCalledAndOrderDoesNotExist_thenThrowOrderNotFoundException() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID, USER_ID))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining(ORDER_ID.toString());
  }

  @Test
  void whenGetOrderByIdCalledAndOrderBelongsToDifferentUser_thenThrowAccessDeniedException() {
    Order order = buildOrder().toBuilder().userId(randomUUID()).build();
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID, USER_ID))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("authenticated user");
  }

  @Test
  void whenGetOrdersForUserCalled_thenReturnMappedOrders() {
    Order orderOne = buildOrder();
    Order orderTwo = buildOrder().toBuilder().id(randomUUID()).build();
    OrderResponse orderResponseOne = buildOrderResponse();
    OrderResponse orderResponseTwo =
        buildOrderResponse().toBuilder().orderId(orderTwo.getId()).build();

    when(orderRepository.findAllByUserId(USER_ID)).thenReturn(List.of(orderOne, orderTwo));
    when(orderMapper.toOrderResponse(orderOne)).thenReturn(orderResponseOne);
    when(orderMapper.toOrderResponse(orderTwo)).thenReturn(orderResponseTwo);

    List<OrderResponse> response = orderService.getOrdersForUser(USER_ID);

    assertThat(response).containsExactly(orderResponseOne, orderResponseTwo);
    verify(orderRepository).findAllByUserId(USER_ID);
  }

  @Test
  void whenGetOrdersForUserCalledAndNoOrdersExist_thenReturnEmptyList() {
    when(orderRepository.findAllByUserId(USER_ID)).thenReturn(List.of());

    List<OrderResponse> response = orderService.getOrdersForUser(USER_ID);

    assertThat(response).isEmpty();
  }

  @Test
  void whenUpdateOrderStatusCalled_thenSaveOrderWithNewStatusAndUpdatedTimestamp() {
    Instant originalUpdatedAt = Instant.parse("2025-01-01T10:00:00Z");
    Order existingOrder =
        buildOrder().toBuilder().status(CREATED).updatedAt(originalUpdatedAt).build();
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(PAID).build();

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

    orderService.updateOrderStatus(ORDER_ID, request);

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());
    assertThat(orderCaptor.getValue().getId()).isEqualTo(ORDER_ID);
    assertThat(orderCaptor.getValue().getStatus()).isEqualTo(PAID);
    assertThat(orderCaptor.getValue().getUpdatedAt()).isAfter(originalUpdatedAt);
    assertThat(orderCaptor.getValue().getCreatedAt()).isEqualTo(existingOrder.getCreatedAt());
  }

  @Test
  void whenUpdateOrderStatusCalledAndOrderDoesNotExist_thenThrowOrderNotFoundException() {
    OrderUpdateRequest request = OrderUpdateRequest.builder().newStatus(PAID).build();
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.updateOrderStatus(ORDER_ID, request))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessageContaining(ORDER_ID.toString());

    verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.isA(Order.class));
  }

  private Order buildOrder() {
    return Order.builder()
        .id(ORDER_ID)
        .userId(USER_ID)
        .status(CREATED)
        .totalAmount(BigDecimal.valueOf(25.50))
        .items(List.of())
        .createdAt(Instant.parse("2025-01-01T09:00:00Z"))
        .updatedAt(Instant.parse("2025-01-01T10:00:00Z"))
        .build();
  }

  private OrderResponse buildOrderResponse() {
    return OrderResponse.builder()
        .orderId(ORDER_ID)
        .userId(USER_ID)
        .orderStatus(CREATED)
        .totalPrice(BigDecimal.valueOf(25.50))
        .items(List.of())
        .createdAt(Instant.parse("2025-01-01T09:00:00Z"))
        .build();
  }
}
