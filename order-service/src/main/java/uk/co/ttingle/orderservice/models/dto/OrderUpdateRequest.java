package uk.co.ttingle.orderservice.models.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.co.ttingle.orderservice.enums.OrderStatus;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderUpdateRequest {
  @NotNull private OrderStatus newStatus;
}
