package productservice.models.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
  private String category;

  @Positive(message = "Minimum price must be positive") private BigDecimal minPrice;

  @Positive(message = "Maximum price must be positive") private BigDecimal maxPrice;

  private String search;

  private Boolean active = true;
}
