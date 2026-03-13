package orderservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

  @Id @GeneratedValue private UUID id;

  private UUID productId;

  private String productNameAtPurchase;

  private Integer quantity;

  private BigDecimal priceAtPurchase;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;
}
