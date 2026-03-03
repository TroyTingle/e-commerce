package productservice.models.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

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
