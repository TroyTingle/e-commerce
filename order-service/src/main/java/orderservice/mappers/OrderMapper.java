package orderservice.mappers;

import orderservice.models.Order;
import orderservice.models.OrderItem;
import orderservice.models.dto.OrderItemResponse;
import orderservice.models.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

  public OrderResponse toOrderResponse(Order order) {
    return OrderResponse.builder()
        .orderId(order.getId())
        .userId(order.getUserId())
        .orderStatus(order.getStatus())
        .totalPrice(order.getTotalAmount())
        .items(order.getItems()
            .stream()
            .map(this::toOrderItemResponse)
            .toList())
        .createdAt(order.getCreatedAt())
        .build();
  }

  private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
    return OrderItemResponse.builder()
        .productName(orderItem.getProductNameAtPurchase())
        .quantity(orderItem.getQuantity())
        .build();
  }
}
