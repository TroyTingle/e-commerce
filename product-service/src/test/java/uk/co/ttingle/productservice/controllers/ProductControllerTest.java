package uk.co.ttingle.productservice.controllers;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductDto;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductPage;
import uk.co.ttingle.productservice.services.ProductService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  private static final UUID PRODUCT_ID = randomUUID();
  private static final String PRODUCT_SKU = "TEST-SKU-123";

  @Mock private ProductService productService;

  @InjectMocks private ProductController productController;

  @Test
  void whenGetAllProductsCalled_thenReturnPageOfProductDto() {
    Pageable pageable = Pageable.ofSize(5);
    List<ProductDto> productDtoList = Instancio.ofList(ProductDto.class).size(5).create();
    ProductPage productPage =
        Instancio.of(ProductPage.class)
            .set(field(ProductPage::getContent), productDtoList)
            .set(field(ProductPage::getTotalElements), (long) productDtoList.size())
            .create();

    when(productService.getAllProducts("category", ONE, TEN, "name", true, pageable))
        .thenReturn(productPage);

    ResponseEntity<ProductPage> response =
        productController.getAllProducts("category", ONE, TEN, "name", true, pageable);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(productPage);
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getTotalElements()).isEqualTo(productDtoList.size());
  }

  @Test
  void whenGetProductByIdCalled_thenReturnProductDto() {
    ProductDto productDto = Instancio.of(ProductDto.class).create();

    when(productService.getProductById(PRODUCT_ID)).thenReturn(productDto);

    ResponseEntity<ProductDto> response = productController.getProductById(PRODUCT_ID);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isInstanceOf(ProductDto.class).isEqualTo(productDto);
  }

  @Test
  void whenGetProductBySkuCalled_thenReturnProductDto() {
    ProductDto productDto = Instancio.of(ProductDto.class).create();

    when(productService.getProductBySku(PRODUCT_SKU)).thenReturn(productDto);

    ResponseEntity<ProductDto> response = productController.getProductBySku(PRODUCT_SKU);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isInstanceOf(ProductDto.class).isEqualTo(productDto);
  }
}
