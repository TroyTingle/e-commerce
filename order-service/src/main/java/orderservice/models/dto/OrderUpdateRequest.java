package orderservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import orderservice.enums.OrderStatus;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderUpdateRequest {
  private OrderStatus newStatus;
}
