package orderservice.integration.testdata;

import static java.time.Instant.now;
import static org.instancio.Select.field;

import ecommerce.product.v1.ProductRequest;
import ecommerce.product.v1.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import orderservice.enums.OrderStatus;
import orderservice.models.Order;
import orderservice.models.OrderItem;
import orderservice.repositories.OrderRepository;
import org.instancio.Instancio;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@RequiredArgsConstructor
public class OrderData {

  private final OrderRepository orderRepository;

  public ProductRequest productRequest(UUID productId) {
    return ProductRequest.newBuilder().setProductId(productId.toString()).build();
  }

  public ProductResponse productResponse(UUID productId, String name, String price) {
    return ProductResponse.newBuilder()
        .setId(productId.toString())
        .setName(name)
        .setDescription(name + " description")
        .setPrice(new BigDecimal(price).movePointRight(2).longValueExact())
        .setCurrency("GBP")
        .setSku(name.toUpperCase())
        .setCategory("Accessories")
        .build();
  }

  public Order createOrder(
      UUID userId, String productName, int quantity, String itemPrice, OrderStatus status) {
    Order order =
        Instancio.of(Order.class)
            .ignore(field(Order::getId))
            .set(field(Order::getUserId), userId)
            .set(field(Order::getStatus), status)
            .set(field(Order::getCreatedAt), now())
            .set(field(Order::getUpdatedAt), now())
            .set(
                field(Order::getTotalAmount),
                new BigDecimal(itemPrice).multiply(BigDecimal.valueOf(quantity)))
            .set(
                field(Order::getItems),
                List.of(
                    Instancio.of(OrderItem.class)
                        .set(field(OrderItem::getProductId), UUID.randomUUID())
                        .set(field(OrderItem::getProductNameAtPurchase), productName)
                        .set(field(OrderItem::getQuantity), quantity)
                        .set(field(OrderItem::getPriceAtPurchase), new BigDecimal(itemPrice))
                        .create()))
            .create();
    return orderRepository.save(order);
  }
}
