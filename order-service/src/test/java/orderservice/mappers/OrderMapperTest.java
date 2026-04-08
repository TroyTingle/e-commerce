package orderservice.mappers;

import static java.time.Instant.parse;
import static orderservice.enums.OrderStatus.CREATED;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import orderservice.clients.ProductServiceClient;
import orderservice.models.Order;
import orderservice.models.OrderItem;
import orderservice.models.dto.OrderItemRequest;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ttingle.commonlib.dto.ProductDto;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

  @Mock private ProductServiceClient productServiceClient;

  @InjectMocks private OrderMapper orderMapper;

  @Test
  void toNewOrder_fetchesProductsAndCalculatesTotalPrice() {
    UUID userId = UUID.randomUUID();
    UUID keyboardId = UUID.randomUUID();
    UUID mouseId = UUID.randomUUID();
    OrderRequestDto request =
        new OrderRequestDto(
            List.of(new OrderItemRequest(keyboardId, 2), new OrderItemRequest(mouseId, 3)));

    when(productServiceClient.getProductByUuid(keyboardId))
        .thenReturn(product(keyboardId, "Keyboard", "10.50"));
    when(productServiceClient.getProductByUuid(mouseId))
        .thenReturn(product(mouseId, "Mouse", "5.25"));

    Order order = orderMapper.toNewOrder(request, userId);

    assertEquals(userId, order.getUserId());
    assertEquals(CREATED, order.getStatus());
    assertEquals(new BigDecimal("36.75"), order.getTotalAmount());
    assertEquals(2, order.getItems().size());

    OrderItem firstItem = order.getItems().getFirst();
    assertEquals(keyboardId, firstItem.getProductId());
    assertEquals("Keyboard", firstItem.getProductNameAtPurchase());
    assertEquals(2, firstItem.getQuantity());
    assertEquals(new BigDecimal("10.50"), firstItem.getPriceAtPurchase());

    assertNotNull(order.getCreatedAt());
    assertNotNull(order.getUpdatedAt());
    assertFalse(order.getUpdatedAt().isBefore(order.getCreatedAt()));
  }

  @Test
  void toNewOrder_returnsZeroWhenNoItemsArePresent() {
    OrderRequestDto request = new OrderRequestDto(List.of());

    Order order = orderMapper.toNewOrder(request, UUID.randomUUID());

    assertEquals(BigDecimal.ZERO, order.getTotalAmount());
    assertTrue(order.getItems().isEmpty());
  }

  @Test
  void toOrderResponse_mapsOrderAndItemFields() {
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Order order =
        Order.builder()
            .id(orderId)
            .userId(userId)
            .status(CREATED)
            .totalAmount(new BigDecimal("42.00"))
            .createdAt(parse("2026-04-01T09:00:00Z"))
            .items(
                List.of(
                    OrderItem.builder()
                        .productId(UUID.randomUUID())
                        .productNameAtPurchase("Mechanical Keyboard")
                        .quantity(2)
                        .priceAtPurchase(new BigDecimal("21.00"))
                        .build()))
            .build();

    OrderResponse response = orderMapper.toOrderResponse(order);

    assertEquals(orderId, response.getOrderId());
    assertEquals(userId, response.getUserId());
    assertEquals(CREATED, response.getOrderStatus());
    assertEquals(new BigDecimal("42.00"), response.getTotalPrice());
    assertEquals(parse("2026-04-01T09:00:00Z"), response.getCreatedAt());
    assertEquals(1, response.getItems().size());
    assertEquals("Mechanical Keyboard", response.getItems().getFirst().getProductName());
    assertEquals(2, response.getItems().getFirst().getQuantity());
  }

  private static ProductDto product(UUID id, String name, String price) {
    return Instancio.of(ProductDto.class)
        .set(field(ProductDto::getId), id)
        .set(field(ProductDto::getName), name)
        .set(field(ProductDto::getPrice), new BigDecimal(price))
        .create();
  }
}
