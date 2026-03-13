package orderservice.models;

import static jakarta.persistence.CascadeType.ALL;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import orderservice.enums.OrderStatus;

@Entity
@Table(name = "orders")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private UUID userId;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  private BigDecimal totalAmount;

  private Instant createdAt;

  private Instant updatedAt;

  @OneToMany(mappedBy = "order", cascade = ALL)
  private List<OrderItem> items;
}
