package productservice.models.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

  private UUID id;
  private String name;
  private String description;
  private BigDecimal price;
  private String currency;
  private String sku;
  private String category;
}
