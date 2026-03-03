package productservice.mappers;


import org.springframework.stereotype.Component;
import productservice.models.Product;
import productservice.models.dto.ProductDto;

@Component
public class ProductMapper {

  public ProductDto toProductDto(Product product) {
    return ProductDto.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .sku(product.getSku())
        .category(product.getCategory().getName())
        .build();
  }
}
