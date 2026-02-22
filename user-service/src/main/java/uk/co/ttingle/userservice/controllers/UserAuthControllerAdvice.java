package uk.co.ttingle.userservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.co.ttingle.commonlib.dto.ExceptionDto;
import uk.co.ttingle.userservice.exceptions.EmailConflictException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ControllerAdvice
public class UserAuthControllerAdvice {


  @ExceptionHandler(EmailConflictException.class)
  public ResponseEntity<ExceptionDto> handleGlobalException(EmailConflictException ex) {
    return ResponseEntity.status(CONFLICT).body(new ExceptionDto(ex.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ExceptionDto> handleGlobalException(BadCredentialsException ex) {
    return ResponseEntity.status(UNAUTHORIZED).body(new ExceptionDto(ex.getMessage()));
  }




  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionDto> handleGlobalException(Exception ex) {
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ExceptionDto(ex.getMessage()));
  }
}
