package uk.co.ttingle.userservice.controllers;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.ttingle.userservice.models.dto.AuthResponse;
import uk.co.ttingle.userservice.models.dto.LoginRequest;
import uk.co.ttingle.userservice.services.UserAuthService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(MockitoExtension.class)
class UserAuthControllerTest {

  @Mock
  private UserAuthService userAuthService;

  @InjectMocks
  private UserAuthController userAuthController;


  @Test
  void testLoginCalled_thenAuthResponseReturned() {
    AuthResponse authResponse = Instancio.of(AuthResponse.class).create();
    LoginRequest loginRequest = Instancio.of(LoginRequest.class).create();

    when(userAuthService.loginUser(loginRequest)).thenReturn(authResponse);

    ResponseEntity<AuthResponse> response = userAuthController.login(loginRequest);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isInstanceOf(AuthResponse.class);
    assertThat(response.getBody()).isEqualTo(authResponse);
  }
}
