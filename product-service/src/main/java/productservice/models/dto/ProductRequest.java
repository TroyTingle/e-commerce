package productservice.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class ProductRequest {

  @NotBlank private String name;
  @NotBlank private String description;
  @NotNull @Positive private BigDecimal price;
  @NotBlank private String sku;
  @NotBlank @PositiveOrZero private Integer inventoryQuantity;
  @NotBlank private String categoryName;
}
