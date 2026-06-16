package uk.co.ttingle.inventoryservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.co.ttingle.inventoryservice.exceptions.InvalidStockAdjustmentException;
import uk.co.ttingle.inventoryservice.exceptions.InventoryItemNotFoundException;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.ExceptionDto;

@Tag("unit")
class InventoryServiceControllerAdviceTest {

  private final InventoryServiceControllerAdvice controllerAdvice =
      new InventoryServiceControllerAdvice();

  @Test
  void whenUnhandledExceptionCaught_thenInternalServerErrorReturned() {
    ResponseEntity<ExceptionDto> response =
        controllerAdvice.handleAllExceptions(new Exception("Unknown error"));

    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().getMessage()).isEqualTo("Unknown error");
  }

  @Test
  void whenValidationExceptionCaught_thenValidationErrorsReturned() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "stockAdjustmentRequest");
    bindingResult.addError(
        new FieldError("stockAdjustmentRequest", "quantityDelta", "must not be null"));
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);
    when(exception.getMessage()).thenReturn("Validation failed");

    ResponseEntity<ExceptionDto> response =
        controllerAdvice.handleMethodArgumentNotValidException(exception);

    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
    assertThat(response.getBody().getValidationErrors())
        .containsEntry("quantityDelta", "must not be null");
  }

  @Test
  void whenUnreadableMessageCaught_thenBadRequestReturned() {
    ResponseEntity<ExceptionDto> response =
        controllerAdvice.handleHttpMessageNotReadableException(
            new HttpMessageNotReadableException("Malformed JSON", mock(HttpInputMessage.class)));

    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertThat(response.getBody().getMessage()).isEqualTo("Malformed JSON");
  }

  @Test
  void whenInvalidStockAdjustmentCaught_thenBadRequestReturned() {
    ResponseEntity<ExceptionDto> response =
        controllerAdvice.handleInvalidStockAdjustmentException(
            new InvalidStockAdjustmentException("Invalid adjustment"));

    assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
    assertThat(response.getBody().getMessage()).isEqualTo("Invalid adjustment");
  }

  @Test
  void whenInventoryItemNotFoundCaught_thenNotFoundReturned() {
    ResponseEntity<ExceptionDto> response =
        controllerAdvice.handleInventoryItemNotFoundException(
            new InventoryItemNotFoundException("Not found"));

    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    assertThat(response.getBody().getMessage()).isEqualTo("Not found");
  }
}
