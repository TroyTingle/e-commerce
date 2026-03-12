package productservice.controllers;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import productservice.models.dto.InventoryUpdateRequest;
import productservice.models.dto.ProductDto;
import productservice.models.dto.ProductRequest;
import productservice.services.ProductService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

  private static final UUID PRODUCT_ID = randomUUID();

  @Mock private ProductService productService;

  @InjectMocks private AdminProductController adminProductController;

  @Test
  void whenCreateProductCalled_thenProductDtoReturned() {
    ProductRequest request = Instancio.of(ProductRequest.class).create();
    ProductDto productDto = Instancio.of(ProductDto.class).create();

    when(productService.createProduct(request)).thenReturn(productDto);

    ResponseEntity<ProductDto> response = adminProductController.createProduct(request);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isInstanceOf(ProductDto.class).isEqualTo(productDto);
  }

  @Test
  void whenUpdateProductCalled_thenProductDtoReturned() {
    ProductRequest request = Instancio.of(ProductRequest.class).create();
    ProductDto productDto = Instancio.of(ProductDto.class).create();

    when(productService.updateProduct(PRODUCT_ID, request)).thenReturn(productDto);

    ResponseEntity<ProductDto> response = adminProductController.updateProduct(PRODUCT_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isInstanceOf(ProductDto.class).isEqualTo(productDto);
  }

  @Test
  void whenDeleteProductCalled_thenNoResponseBodyReturned() {
    doNothing().when(productService).deactivateProduct(PRODUCT_ID);

    ResponseEntity<Void> response = adminProductController.deleteProduct(PRODUCT_ID);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void whenUpdateInventoryCalled_thenNoResponseBodyReturned() {
    InventoryUpdateRequest request = Instancio.of(InventoryUpdateRequest.class).create();

    doNothing().when(productService).updateInventory(PRODUCT_ID, request);

    ResponseEntity<Void> response = adminProductController.updateInventory(PRODUCT_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }
}
