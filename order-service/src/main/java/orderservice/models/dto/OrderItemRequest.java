package orderservice.models.dto;

import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {

  @org.hibernate.validator.constraints.UUID private UUID productId;
  @Positive private Integer quantity;
}
