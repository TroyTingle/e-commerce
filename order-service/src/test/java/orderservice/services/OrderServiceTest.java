package orderservice.services;

import static java.time.Instant.now;
import static orderservice.enums.OrderStatus.CANCELLED;
import static orderservice.enums.OrderStatus.CREATED;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private OrderMapper orderMapper;

  @InjectMocks private OrderService orderService;

  @Test
  void createOrder_mapsAndPersistsOrder() {
    OrderRequestDto request = new OrderRequestDto(List.of());
    UUID userId = UUID.randomUUID();
    Order mappedOrder = baseOrder(userId);
    Order savedOrder = mappedOrder.toBuilder().id(UUID.randomUUID()).build();
    OrderResponse expectedResponse = OrderResponse.builder().orderId(savedOrder.getId()).build();

    when(orderMapper.toNewOrder(request, userId)).thenReturn(mappedOrder);
    when(orderRepository.save(mappedOrder)).thenReturn(savedOrder);
    when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expectedResponse);

    OrderResponse response = orderService.createOrder(request, userId);

    assertSame(expectedResponse, response);
    verify(orderMapper).toNewOrder(request, userId);
    verify(orderRepository).save(mappedOrder);
    verify(orderMapper).toOrderResponse(savedOrder);
  }

  @Test
  void getOrderById_returnsMappedResponseWhenOrderExists() {
    Order order = baseOrder(UUID.randomUUID());
    UUID orderId = order.getId();

    OrderResponse expectedResponse = OrderResponse.builder().orderId(orderId).build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderMapper.toOrderResponse(order)).thenReturn(expectedResponse);

    OrderResponse response = orderService.getOrderById(orderId);

    assertSame(expectedResponse, response);
    verify(orderRepository).findById(orderId);
    verify(orderMapper).toOrderResponse(order);
  }

  @Test
  void getOrderById_throwsWhenOrderDoesNotExist() {
    UUID orderId = UUID.randomUUID();
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    OrderNotFoundException exception =
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));

    assertEquals(
        String.format("Order could not be found with id %s", orderId), exception.getMessage());
    verify(orderMapper, never()).toOrderResponse(any());
  }

  @Test
  void getOrdersForUser_mapsEveryOrderFromRepository() {
    UUID userId = UUID.randomUUID();
    Order firstOrder = baseOrder(userId);
    Order secondOrder = baseOrder(userId);
    OrderResponse firstResponse = OrderResponse.builder().orderId(firstOrder.getId()).build();
    OrderResponse secondResponse = OrderResponse.builder().orderId(secondOrder.getId()).build();

    when(orderRepository.findAllByUserId(userId)).thenReturn(List.of(firstOrder, secondOrder));
    when(orderMapper.toOrderResponse(firstOrder)).thenReturn(firstResponse);
    when(orderMapper.toOrderResponse(secondOrder)).thenReturn(secondResponse);

    List<OrderResponse> responses = orderService.getOrdersForUser(userId);

    assertEquals(List.of(firstResponse, secondResponse), responses);
    verify(orderRepository).findAllByUserId(userId);
    verify(orderMapper).toOrderResponse(firstOrder);
    verify(orderMapper).toOrderResponse(secondOrder);
  }

  @Test
  void updateOrderStatus_persistsOrderWithNewStatusAndUpdatedTimestamp() {
    Order existingOrder = baseOrder(UUID.randomUUID());
    UUID orderId = existingOrder.getId();
    Instant originalUpdatedAt = existingOrder.getUpdatedAt();

    OrderUpdateRequest updateRequest = OrderUpdateRequest.builder().newStatus(CANCELLED).build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

    orderService.updateOrderStatus(orderId, updateRequest);

    ArgumentCaptor<Order> savedOrderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(savedOrderCaptor.capture());

    Order savedOrder = savedOrderCaptor.getValue();
    assertEquals(orderId, savedOrder.getId());
    assertEquals(CANCELLED, savedOrder.getStatus());
    assertEquals(existingOrder.getCreatedAt(), savedOrder.getCreatedAt());
    assertNotNull(savedOrder.getUpdatedAt());
    assertTrue(savedOrder.getUpdatedAt().isAfter(originalUpdatedAt));
  }

  @Test
  void updateOrderStatus_throwsWhenOrderDoesNotExist() {
    UUID orderId = UUID.randomUUID();
    OrderUpdateRequest updateRequest = OrderUpdateRequest.builder().newStatus(CANCELLED).build();

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class, () -> orderService.updateOrderStatus(orderId, updateRequest));
    verify(orderRepository, never()).save(any());
  }

  private static Order baseOrder(UUID userId) {
    return Instancio.of(Order.class)
        .set(field(Order::getUserId), userId)
        .set(field(Order::getStatus), CREATED)
        .set(field(Order::getCreatedAt), now())
        .set(field(Order::getUpdatedAt), now())
        .create();
  }
}
