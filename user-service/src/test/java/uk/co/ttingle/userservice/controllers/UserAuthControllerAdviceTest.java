package uk.co.ttingle.userservice.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import uk.co.ttingle.commonlib.dto.ExceptionDto;
import uk.co.ttingle.userservice.exceptions.EmailConflictException;

@ExtendWith(MockitoExtension.class)
class UserAuthControllerAdviceTest {

  @InjectMocks private UserAuthControllerAdvice userAuthControllerAdvice;

  @Test
  void handleEmailConflictExceptionIsCalled_thenReturnConflictResponse() {
    EmailConflictException exception = new EmailConflictException("Email already exists");

    ResponseEntity<ExceptionDto> response =
        userAuthControllerAdvice.handleEmailConflictException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Email already exists");
  }

  @Test
  void handleBadCredentialsExceptionExceptionIsCalled_thenReturnUnauthorizedResponse() {
    BadCredentialsException exception = new BadCredentialsException("Bad credentials");

    ResponseEntity<ExceptionDto> response =
        userAuthControllerAdvice.handleBadCredentialsExceptionException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(UNAUTHORIZED);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Bad credentials");
  }

  @Test
  void handleGlobalExceptionIsCalled_thenReturnISEResponse() {
    Exception exception = new Exception("Unknown error");

    ResponseEntity<ExceptionDto> response =
        userAuthControllerAdvice.handleGlobalException(exception);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isInstanceOf(ExceptionDto.class);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Unknown error");
  }
}
