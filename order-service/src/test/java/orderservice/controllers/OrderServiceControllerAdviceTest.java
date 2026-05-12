package orderservice.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import orderservice.exceptions.OrderNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import uk.co.ttingle.commonlib.dto.ExceptionDto;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class OrderServiceControllerAdviceTest {

  @InjectMocks private OrderServiceControllerAdvice orderServiceControllerAdvice;

  @Test
  void handleGlobalExceptionIsCalled_thenReturnISEResponse() {
    Exception exception = new Exception("Unknown error");

    ResponseEntity<ExceptionDto> response =
        orderServiceControllerAdvice.handleAllExceptions(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Unknown error");
  }

  @Test
  void handleOrderNotFoundExceptionIsCalled_thenReturnNotFoundResponse() {
    OrderNotFoundException exception = new OrderNotFoundException("Not Found");

    ResponseEntity<ExceptionDto> response =
        orderServiceControllerAdvice.handleOrderNotFoundException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
  }

  @Test
  void handleAccessDeniedExceptionIsCalled_thenReturnForbiddenResponse() {
    AccessDeniedException exception = new AccessDeniedException("Forbidden");

    ResponseEntity<ExceptionDto> response =
        orderServiceControllerAdvice.handleAccessDeniedException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    Assertions.assertNotNull(response.getBody());
    assertThat(response.getBody().getMessage()).isEqualTo("Forbidden");
  }
}
