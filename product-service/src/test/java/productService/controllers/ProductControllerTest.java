package productService.controllers;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import productservice.controllers.ProductController;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductSearchRequest;
import productservice.services.ProductService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  private static final UUID PRODUCT_ID = randomUUID();
  private static final String PRODUCT_SKU = "TEST-SKU-123";

  @Mock private ProductService productService;

  @InjectMocks private ProductController productController;

  @Test
  void whenGetAllProductsCalled_thenReturnPageOfProductDto() {
    ProductSearchRequest request = Instancio.of(ProductSearchRequest.class).create();
    Pageable pageable = Pageable.ofSize(5);
    List<ProductDto> productDtoList = Instancio.ofList(ProductDto.class).size(5).create();
    Page<ProductDto> productDtoPage = new PageImpl<>(productDtoList);

    when(productService.getAllProducts(request, pageable)).thenReturn(productDtoPage);

    ResponseEntity<Page<ProductDto>> response = productController.getAllProducts(request, pageable);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(productDtoPage);
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
