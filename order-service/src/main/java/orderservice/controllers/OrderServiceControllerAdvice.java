package orderservice.controllers;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import orderservice.exceptions.OrderNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.co.ttingle.commonlib.dto.ExceptionDto;

@Slf4j
@RestControllerAdvice
public class OrderServiceControllerAdvice {

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

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ExceptionDto> handleOrderNotFoundException(
      final OrderNotFoundException ex) {
    return ResponseEntity.status(NOT_FOUND)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ExceptionDto> handleAccessDeniedException(final AccessDeniedException ex) {
    return ResponseEntity.status(FORBIDDEN)
        .body(ExceptionDto.builder().message(ex.getMessage()).build());
  }
}
