package productservice.mappers;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import org.springframework.stereotype.Component;
import productservice.models.Category;
import productservice.models.Product;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;

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

  public Product toNewProduct(ProductRequest request, Category category) {
    return Product.builder()
        .id(randomUUID())
        .name(request.getName())
        .description(request.getDescription())
        .price(request.getPrice())
        .sku(request.getSku())
        .inventoryQuantity(request.getInventoryQuantity())
        .active(true)
        .category(category)
        .createdAt(now())
        .updatedAt(now())
        .build();
  }

  public Product toUpdatedProduct(
      Product existingProduct, ProductRequest request, Category category) {
    return existingProduct.toBuilder()
        .name(request.getName())
        .description(request.getDescription())
        .price(request.getPrice())
        .sku(request.getSku())
        .inventoryQuantity(request.getInventoryQuantity())
        .active(existingProduct.getActive())
        .category(category)
        .updatedAt(now())
        .build();
  }
}
