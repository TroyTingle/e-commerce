package orderservice.mappers;

import static java.util.UUID.randomUUID;
import static orderservice.enums.OrderStatus.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import orderservice.clients.ProductServiceClient;
import orderservice.models.Order;
import orderservice.models.OrderItem;
import orderservice.models.dto.OrderItemRequest;
import orderservice.models.dto.OrderRequestDto;
import orderservice.models.dto.OrderResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ttingle.commonlib.dto.ProductDto;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

  private static final UUID USER_ID = randomUUID();
  private static final UUID ORDER_ID = randomUUID();
  private static final UUID KEYBOARD_ID = randomUUID();
  private static final UUID MOUSE_ID = randomUUID();

  @Mock private ProductServiceClient productServiceClient;

  @Test
  void whenToOrderResponseCalled_thenFieldsAreMapped() {
    OrderItem item =
        OrderItem.builder()
            .productId(KEYBOARD_ID)
            .productNameAtPurchase("Keyboard")
            .quantity(2)
            .priceAtPurchase(BigDecimal.valueOf(10.99))
            .build();
    Order order =
        Order.builder()
            .id(ORDER_ID)
            .userId(USER_ID)
            .status(CREATED)
            .totalAmount(BigDecimal.valueOf(21.98))
            .items(List.of(item))
            .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
            .build();
    OrderMapper orderMapper = new OrderMapper(productServiceClient);

    OrderResponse result = orderMapper.toOrderResponse(order);

    assertThat(result.getOrderId()).isEqualTo(ORDER_ID);
    assertThat(result.getUserId()).isEqualTo(USER_ID);
    assertThat(result.getOrderStatus()).isEqualTo(CREATED);
    assertThat(result.getTotalPrice()).isEqualByComparingTo("21.98");
    assertThat(result.getCreatedAt()).isEqualTo(Instant.parse("2025-01-01T10:00:00Z"));
    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getItems().getFirst().getProductName()).isEqualTo("Keyboard");
    assertThat(result.getItems().getFirst().getQuantity()).isEqualTo(2);
  }

  @Test
  void whenToNewOrderCalled_thenBuildExpectedOrderAndCalculateTotal() {
    OrderItemRequest keyboard = new OrderItemRequest(KEYBOARD_ID, 2);
    OrderItemRequest mouse = new OrderItemRequest(MOUSE_ID, 3);
    OrderRequestDto request = new OrderRequestDto(List.of(keyboard, mouse));
    ProductDto keyboardProduct = buildProduct(KEYBOARD_ID, "Keyboard", BigDecimal.valueOf(10.99));
    ProductDto mouseProduct = buildProduct(MOUSE_ID, "Mouse", BigDecimal.valueOf(5.25));
    OrderMapper orderMapper = new OrderMapper(productServiceClient);

    when(productServiceClient.getProductByUuid(KEYBOARD_ID)).thenReturn(keyboardProduct);
    when(productServiceClient.getProductByUuid(MOUSE_ID)).thenReturn(mouseProduct);

    Instant before = Instant.now();
    Order result = orderMapper.toNewOrder(request, USER_ID);
    Instant after = Instant.now();

    assertThat(result.getUserId()).isEqualTo(USER_ID);
    assertThat(result.getStatus()).isEqualTo(CREATED);
    assertThat(result.getTotalAmount()).isEqualByComparingTo("37.73");
    assertThat(result.getCreatedAt()).isBetween(before, after);
    assertThat(result.getUpdatedAt()).isBetween(before, after);
    assertThat(result.getItems()).hasSize(2);
    assertThat(result.getItems()).allSatisfy(item -> assertThat(item.getOrder()).isSameAs(result));
    assertThat(result.getItems().getFirst().getProductId()).isEqualTo(KEYBOARD_ID);
    assertThat(result.getItems().getFirst().getProductNameAtPurchase()).isEqualTo("Keyboard");
    assertThat(result.getItems().getFirst().getPriceAtPurchase()).isEqualByComparingTo("10.99");
    assertThat(result.getItems().getFirst().getQuantity()).isEqualTo(2);
    assertThat(result.getItems().get(1).getProductId()).isEqualTo(MOUSE_ID);
    assertThat(result.getItems().get(1).getProductNameAtPurchase()).isEqualTo("Mouse");
    assertThat(result.getItems().get(1).getPriceAtPurchase()).isEqualByComparingTo("5.25");
    assertThat(result.getItems().get(1).getQuantity()).isEqualTo(3);
  }

  @Test
  void whenToNewOrderCalledWithNoItems_thenReturnZeroTotalAndDoNotCallProductService() {
    OrderMapper orderMapper = new OrderMapper(productServiceClient);

    Order result = orderMapper.toNewOrder(new OrderRequestDto(List.of()), USER_ID);

    assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.getItems()).isEmpty();
    verify(productServiceClient, never()).getProductByUuid(org.mockito.ArgumentMatchers.any());
  }

  private ProductDto buildProduct(UUID id, String name, BigDecimal price) {
    return ProductDto.builder()
        .id(id)
        .name(name)
        .description(name + " description")
        .price(price)
        .currency("GBP")
        .sku(name.toUpperCase() + "-001")
        .category("Accessories")
        .build();
  }
}
