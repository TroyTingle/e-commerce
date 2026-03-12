package productservice.mappers;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import productservice.models.Category;
import productservice.models.Product;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

  private static final ProductMapper productMapper = new ProductMapper();

  @Test
  void whenToProductDtoCalled_thenFieldsAreMapped() {
    Category category = Category.builder().id(randomUUID()).name("Books").build();
    Product product =
        Product.builder()
            .id(randomUUID())
            .name("Refactoring")
            .description("Book")
            .price(BigDecimal.valueOf(29.99))
            .sku("BOOK-001")
            .inventoryQuantity(25)
            .active(true)
            .category(category)
            .build();

    ProductDto result = productMapper.toProductDto(product);

    assertThat(result.getId()).isEqualTo(product.getId());
    assertThat(result.getName()).isEqualTo("Refactoring");
    assertThat(result.getDescription()).isEqualTo("Book");
    assertThat(result.getPrice()).isEqualByComparingTo("29.99");
    assertThat(result.getSku()).isEqualTo("BOOK-001");
    assertThat(result.getCategory()).isEqualTo("Books");
  }

  @Test
  void whenToNewProductCalled_thenBuildExpectedProductWithDefaults() {
    ProductRequest request =
        ProductRequest.builder()
            .name("Mouse")
            .description("Wireless Mouse")
            .price(BigDecimal.valueOf(49.99))
            .sku("MOUSE-001")
            .inventoryQuantity(7)
            .categoryName("Electronics")
            .build();
    Category category = Category.builder().id(randomUUID()).name("Electronics").build();

    Instant before = Instant.now();
    Product result = productMapper.toNewProduct(request, category);
    Instant after = Instant.now();

    assertThat(result.getName()).isEqualTo("Mouse");
    assertThat(result.getDescription()).isEqualTo("Wireless Mouse");
    assertThat(result.getPrice()).isEqualByComparingTo("49.99");
    assertThat(result.getSku()).isEqualTo("MOUSE-001");
    assertThat(result.getInventoryQuantity()).isEqualTo(7);
    assertThat(result.getActive()).isTrue();
    assertThat(result.getCategory()).isSameAs(category);
    assertThat(result.getCreatedAt()).isBetween(before, after);
    assertThat(result.getUpdatedAt()).isBetween(before, after);
  }

  @Test
  void whenToUpdatedProductCalled_thenUseRequestAndPreserveExistingActive() {
    Category oldCategory = Category.builder().id(randomUUID()).name("Old").build();
    Product existingProduct =
        Product.builder()
            .id(randomUUID())
            .name("Old Name")
            .description("Old Description")
            .price(BigDecimal.valueOf(9.99))
            .sku("OLD-001")
            .inventoryQuantity(1)
            .active(false)
            .category(oldCategory)
            .createdAt(Instant.parse("2025-01-01T10:00:00Z"))
            .updatedAt(Instant.parse("2025-01-01T10:00:00Z"))
            .build();

    ProductRequest request =
        ProductRequest.builder()
            .name("New Name")
            .description("New Description")
            .price(BigDecimal.valueOf(19.99))
            .sku("NEW-001")
            .inventoryQuantity(99)
            .categoryName("New Category")
            .build();
    Category newCategory = Category.builder().id(randomUUID()).name("New Category").build();

    Instant before = Instant.now();
    Product result = productMapper.toUpdatedProduct(existingProduct, request, newCategory);
    Instant after = Instant.now();

    assertThat(result.getId()).isEqualTo(existingProduct.getId());
    assertThat(result.getName()).isEqualTo("New Name");
    assertThat(result.getDescription()).isEqualTo("New Description");
    assertThat(result.getPrice()).isEqualByComparingTo("19.99");
    assertThat(result.getSku()).isEqualTo("NEW-001");
    assertThat(result.getInventoryQuantity()).isEqualTo(99);
    assertThat(result.getActive()).isFalse();
    assertThat(result.getCategory()).isSameAs(newCategory);
    assertThat(result.getCreatedAt()).isEqualTo(existingProduct.getCreatedAt());
    assertThat(result.getUpdatedAt()).isBetween(before, after);
  }
}
