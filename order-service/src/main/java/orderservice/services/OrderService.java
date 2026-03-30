package orderservice.services;

import static java.time.Instant.now;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import orderservice.exceptions.OrderNotFoundException;
import orderservice.mappers.OrderMapper;
import orderservice.models.Order;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import orderservice.models.dto.OrderUpdateRequest;
import orderservice.repositories.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  public OrderResponse createOrder(OrderRequestDto orderRequest, UUID userId) {
    Order order = orderMapper.toNewOrder(orderRequest, userId);
    return orderMapper.toOrderResponse(orderRepository.save(order));
  }

  public OrderResponse getOrderById(UUID orderId) {
    return orderMapper.toOrderResponse(findOrderByIdOrThrow(orderId));
  }

  public List<OrderResponse> getOrdersForUser(UUID userId) {
    return orderRepository.findAllByUserId(userId).stream()
        .map(orderMapper::toOrderResponse)
        .toList();
  }

  public void updateOrderStatus(UUID orderId, OrderUpdateRequest orderUpdate) {
    Order order = findOrderByIdOrThrow(orderId);

    Order updatedOrder =
        order.toBuilder().status(orderUpdate.getNewStatus()).updatedAt(now()).build();

    orderRepository.save(updatedOrder);
  }

  private Order findOrderByIdOrThrow(UUID orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(
            () ->
                new OrderNotFoundException(
                    String.format("Order could not be found with id %s", orderId)));
  }
}
