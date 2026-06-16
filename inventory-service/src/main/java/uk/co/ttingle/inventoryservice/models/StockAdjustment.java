package uk.co.ttingle.inventoryservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.co.ttingle.inventoryservice.enums.StockAdjustmentReason;

@Entity
@Table(name = "stock_adjustments")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StockAdjustment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID productId;

  @Column(nullable = false)
  private String sku;

  @Column(nullable = false)
  private Integer quantityDelta;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StockAdjustmentReason reason;

  @Column(length = 255)
  private String reference;

  private String createdBy;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
