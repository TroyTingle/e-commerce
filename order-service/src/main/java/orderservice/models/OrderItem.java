package orderservice.models;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  private UUID productId;

  private String productNameAtPurchase;

  private Integer quantity;

  private BigDecimal priceAtPurchase;
}
