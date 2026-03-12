package orderservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

  private UUID orderId;
  private UUID userId;
  private OrderStatus orderStatus;
  private BigDecimal totalPrice;
  private List<OrderItemResponse> items;
  private Instant createdAt;
}
