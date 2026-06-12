package uk.co.ttingle.orderservice.mappers;

import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;
import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;
import static uk.co.ttingle.orderservice.enums.OrderStatus.CREATED;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import uk.co.ttingle.commonlib.dto.ProductDto;
import uk.co.ttingle.orderservice.clients.ProductServiceClient;
import uk.co.ttingle.orderservice.generated.rest.v1.dto.OrderItemRequest;
import uk.co.ttingle.orderservice.generated.rest.v1.dto.OrderItemResponse;
import uk.co.ttingle.orderservice.generated.rest.v1.dto.OrderRequestDto;
import uk.co.ttingle.orderservice.generated.rest.v1.dto.OrderResponse;
import uk.co.ttingle.orderservice.models.Order;
import uk.co.ttingle.orderservice.models.OrderItem;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {

  private final ProductServiceClient productServiceClient;

  public OrderResponse toOrderResponse(Order order) {
    return OrderResponse.builder()
        .orderId(order.getId())
        .userId(order.getUserId())
        .orderStatus(order.getStatus())
        .totalPrice(order.getTotalAmount())
        .items(order.getItems().stream().map(this::toOrderItemResponse).toList())
        .createdAt(ofInstant(order.getCreatedAt(), UTC))
        .build();
  }

  private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
    return OrderItemResponse.builder()
        .productName(orderItem.getProductNameAtPurchase())
        .quantity(orderItem.getQuantity())
        .build();
  }

  public Order toNewOrder(OrderRequestDto orderRequestDto, UUID userId) {
    List<OrderItem> items = toOrderItems(orderRequestDto.getItems());

    return Order.builder()
        .userId(userId)
        .status(CREATED)
        .totalAmount(calculateTotalPrice(items))
        .items(items)
        .createdAt(now())
        .updatedAt(now())
        .build();
  }

  private BigDecimal calculateTotalPrice(List<OrderItem> items) {
    if (items.isEmpty()) {
      return ZERO;
    }

    return items.stream()
        .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(ZERO, BigDecimal::add);
  }

  private List<OrderItem> toOrderItems(List<OrderItemRequest> items) {
    return items.stream()
        .map(
            item -> {
              ProductDto product = productServiceClient.getProductByUuid(item.getProductId());

              return OrderItem.builder()
                  .productId(item.getProductId())
                  .productNameAtPurchase(product.getName())
                  .quantity(item.getQuantity())
                  .priceAtPurchase(product.getPrice())
                  .build();
            })
        .toList();
  }
}
