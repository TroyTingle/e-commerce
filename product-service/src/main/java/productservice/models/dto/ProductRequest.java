package productservice.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

  @NotBlank private String name;
  @NotBlank private String description;
  @NotNull @Positive private BigDecimal price;
  @NotBlank private String currency;
  @NotBlank private String sku;
  @NotNull @PositiveOrZero private Integer inventoryQuantity;
  @NotBlank private String categoryName;
}
