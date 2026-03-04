package productservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.co.ttingle.commonlib.dto.ExceptionDto;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class ProductControllerAdvice {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionDto> handleAllExceptions(Exception ex) {
    log.error("Unhandled exception caught: ", ex);
    return ResponseEntity.status(INTERNAL_SERVER_ERROR)
        .body(ExceptionDto.builder()
            .message(ex.getMessage())
            .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ExceptionDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return ResponseEntity.status(BAD_REQUEST).body(ExceptionDto.builder()
        .message(ex.getMessage())
        .validationErrors(errors)
        .build());
  }
}
