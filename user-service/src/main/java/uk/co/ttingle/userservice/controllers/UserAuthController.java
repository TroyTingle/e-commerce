package uk.co.ttingle.userservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.userservice.models.dto.LoginRequest;
import uk.co.ttingle.userservice.models.dto.RegisterRequest;
import uk.co.ttingle.userservice.models.dto.UserDto;
import uk.co.ttingle.userservice.services.UserAuthService;

import static org.springframework.http.HttpStatus.CREATED;

@RestController("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController {

  private final UserAuthService userAuthService;

  @GetMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(userAuthService.loginUser(loginRequest));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> register(@RequestBody RegisterRequest registerRequest) {
    return ResponseEntity.status(CREATED)
        .body(userAuthService.registerUser(registerRequest));
  }
}
