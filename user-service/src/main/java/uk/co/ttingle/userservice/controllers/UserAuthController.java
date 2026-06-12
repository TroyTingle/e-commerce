package uk.co.ttingle.userservice.controllers;

import static org.springframework.http.HttpStatus.CREATED;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.userservice.generated.rest.v1.AuthenticationApiV1;
import uk.co.ttingle.userservice.generated.rest.v1.dto.AuthResponse;
import uk.co.ttingle.userservice.generated.rest.v1.dto.LoginRequest;
import uk.co.ttingle.userservice.generated.rest.v1.dto.RegisterRequest;
import uk.co.ttingle.userservice.generated.rest.v1.dto.UserDto;
import uk.co.ttingle.userservice.services.UserAuthService;

@RestController
@RequiredArgsConstructor
public class UserAuthController implements AuthenticationApiV1 {

  private final UserAuthService userAuthService;

  @Override
  public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
    return ResponseEntity.ok(userAuthService.loginUser(loginRequest));
  }

  @Override
  public ResponseEntity<UserDto> register(RegisterRequest registerRequest) {
    return ResponseEntity.status(CREATED).body(userAuthService.registerUser(registerRequest));
  }
}
