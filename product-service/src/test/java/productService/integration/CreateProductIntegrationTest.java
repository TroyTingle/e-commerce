package productService.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;

@Tag("integration")
class CreateProductIntegrationTest {

  private static final RestClient restClient = RestClient.create();
  private static final String CREATE_PRODUCT_URL = "http://localhost:8080/api/v1/admin/products";

  @Test
  void createNewProduct_whenCategoryExists_thenReturnProductDto() {
    ProductRequest request = Instancio.of(ProductRequest.class).create();

    ResponseEntity<ProductDto> response =
        restClient
            .post()
            .uri(CREATE_PRODUCT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(ProductDto.class);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody())
        .isInstanceOf(ProductDto.class)
        .extracting(
            ProductDto::getId,
            ProductDto::getName,
            ProductDto::getPrice,
            ProductDto::getDescription)
        .isEqualTo(List.of());
  }

  @Test
  void createNewProduct_whenCategoryDoesNotExist_thenThrowNotFoundException() {
    ProductRequest request = Instancio.of(ProductRequest.class).create();

    assertThatThrownBy(
            () ->
                restClient
                    .post()
                    .uri(CREATE_PRODUCT_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(HttpClientErrorException.class))
        .isInstanceOf(HttpClientErrorException.class)
        .message()
        .contains("No category found with name");
  }
}
