package productservice.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import productservice.integration.testdata.CreateProductData;
import productservice.models.Category;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;

@Tag("integration")
class CreateProductIntegrationTest extends BaseIntegrationTest {

  private static final String PRODUCT_ADMIN_URL = "/api/v1/admin/products";

  @Autowired CreateProductData createProductData;

  @Test
  void createNewProduct_whenCategoryExists_thenReturnProductDto() {
    Category category = createProductData.createCategoryWithName("Test Category");

    ProductRequest request =
        Instancio.of(ProductRequest.class)
            .set(field(ProductRequest::getCategoryName), category.getName())
            .create();

    ResponseEntity<ProductDto> response =
        productServiceClient
            .post()
            .uri(PRODUCT_ADMIN_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(ProductDto.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody())
        .isInstanceOf(ProductDto.class)
        .extracting(
            ProductDto::getName,
            ProductDto::getPrice,
            ProductDto::getCurrency,
            ProductDto::getDescription,
            ProductDto::getCategory,
            ProductDto::getSku)
        .isEqualTo(
            List.of(
                request.getName(),
                request.getPrice(),
                request.getCurrency(),
                request.getDescription(),
                request.getCategoryName(),
                request.getSku()));
  }

  @Test
  void createNewProduct_whenCategoryDoesNotExist_thenThrowNotFoundException() {
    ProductRequest request = Instancio.of(ProductRequest.class).create();

    assertThatThrownBy(
            () ->
                productServiceClient
                    .post()
                    .uri(PRODUCT_ADMIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(HttpClientErrorException.class))
        .isInstanceOf(HttpClientErrorException.class)
        .message()
        .contains("No category found with name");
  }
}
