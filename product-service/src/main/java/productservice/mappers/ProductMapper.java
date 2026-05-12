package productservice.mappers;

import static java.time.Instant.now;

import org.springframework.stereotype.Component;
import productservice.models.Category;
import productservice.models.Product;
import productservice.models.dto.ProductRequest;
import uk.co.ttingle.commonlib.dto.ProductDto;

@Component
public class ProductMapper {

  public ProductDto toProductDto(Product product) {
    return ProductDto.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .currency(product.getCurrency())
        .sku(product.getSku())
        .category(product.getCategory().getName())
        .build();
  }

  public Product toNewProduct(ProductRequest request, Category category) {
    return Product.builder()
        .name(request.getName())
        .description(request.getDescription())
        .price(request.getPrice())
        .currency(request.getCurrency())
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
        .currency(request.getCurrency())
        .sku(request.getSku())
        .inventoryQuantity(request.getInventoryQuantity())
        .active(existingProduct.getActive())
        .category(category)
        .updatedAt(now())
        .build();
  }
}
