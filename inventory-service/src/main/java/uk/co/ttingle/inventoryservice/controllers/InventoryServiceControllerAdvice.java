package uk.co.ttingle.inventoryservice.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.co.ttingle.inventoryservice.exceptions.InvalidStockAdjustmentException;
import uk.co.ttingle.inventoryservice.exceptions.InventoryItemNotFoundException;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.ExceptionDto;

@Slf4j
@RestControllerAdvice
public class InventoryServiceControllerAdvice {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionDto> handleAllExceptions(final Exception ex) {
    log.error("Unhandled exception caught: ", ex);
    return ResponseEntity.status(INTERNAL_SERVER_ERROR)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionDto> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return ResponseEntity.status(BAD_REQUEST)
        .body(ExceptionDto.builder().message(ex.getMessage()).validationErrors(errors).build());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ExceptionDto> handleHttpMessageNotReadableException(
      final HttpMessageNotReadableException ex) {
    return ResponseEntity.status(BAD_REQUEST)
        .body(ExceptionDto.builder().message(ex.getMostSpecificCause().getMessage()).build());
  }

  @ExceptionHandler(InvalidStockAdjustmentException.class)
  public ResponseEntity<ExceptionDto> handleInvalidStockAdjustmentException(
      final InvalidStockAdjustmentException ex) {
    return ResponseEntity.status(BAD_REQUEST)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(InventoryItemNotFoundException.class)
  public ResponseEntity<ExceptionDto> handleInventoryItemNotFoundException(
      final InventoryItemNotFoundException ex) {
    return ResponseEntity.status(NOT_FOUND)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }
}
