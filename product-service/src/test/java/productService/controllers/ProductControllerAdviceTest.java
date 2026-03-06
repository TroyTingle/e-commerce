package productService.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import productservice.controllers.ProductServiceControllerAdvice;
import productservice.exceptions.CategoryNotFoundException;
import productservice.exceptions.DuplicateSkuException;
import productservice.exceptions.ProductNotFoundException;
import uk.co.ttingle.commonlib.dto.ExceptionDto;

@ExtendWith(MockitoExtension.class)
class ProductControllerAdviceTest {

  @InjectMocks private ProductServiceControllerAdvice productControllerAdvice;

  @Test
  void handleGlobalExceptionIsCalled_thenReturnISEResponse() {
    Exception exception = new Exception("Unknown error");

    ResponseEntity<ExceptionDto> response = productControllerAdvice.handleAllExceptions(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Unknown error");
  }

  @Test
  void handleProductNotFoundExceptionIsCalled_thenReturnNotFoundResponse() {
    ProductNotFoundException exception = new ProductNotFoundException("Not Found");

    ResponseEntity<ExceptionDto> response =
        productControllerAdvice.handleProductNotFoundException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
  }

  @Test
  void handleCategoryNotFoundExceptionIsCalled_thenReturnNotFoundResponse() {
    CategoryNotFoundException exception = new CategoryNotFoundException("Not Found");

    ResponseEntity<ExceptionDto> response =
        productControllerAdvice.handleCategoryNotFoundException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
  }

  @Test
  void handleDuplicateSkuExceptionIsCalled_thenReturnNotFoundResponse() {
    DuplicateSkuException exception = new DuplicateSkuException("Sku already exists");

    ResponseEntity<ExceptionDto> response =
        productControllerAdvice.handleDuplicateSkuException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Sku already exists");
  }
}
