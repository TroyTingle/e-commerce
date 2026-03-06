package uk.co.ttingle.userservice.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class LoginRequest {

  @Email @NotBlank private String email;
  @NotBlank private String password;
}
