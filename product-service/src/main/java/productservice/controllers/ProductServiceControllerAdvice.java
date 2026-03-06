package productservice.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import productservice.exceptions.CategoryNotFoundException;
import productservice.exceptions.DuplicateSkuException;
import productservice.exceptions.ProductNotFoundException;
import uk.co.ttingle.commonlib.dto.ExceptionDto;

@Slf4j
@RestControllerAdvice
public class ProductServiceControllerAdvice {

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

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ExceptionDto> handleProductNotFoundException(
      final ProductNotFoundException ex) {
    return ResponseEntity.status(NOT_FOUND)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(CategoryNotFoundException.class)
  public ResponseEntity<ExceptionDto> handleCategoryNotFoundException(
      final CategoryNotFoundException ex) {
    return ResponseEntity.status(NOT_FOUND)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(DuplicateSkuException.class)
  public ResponseEntity<ExceptionDto> handleDuplicateSkuException(final DuplicateSkuException ex) {
    return ResponseEntity.status(CONFLICT)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }
}
