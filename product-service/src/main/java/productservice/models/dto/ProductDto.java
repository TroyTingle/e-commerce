package productservice.models.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ProductDto {

  private UUID id;
  private String name;
  private String description;
  private BigDecimal price;
  private String sku;
  private String category;
}
