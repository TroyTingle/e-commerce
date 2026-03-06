package uk.co.ttingle.userservice.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class RegisterRequest {

  @Email @NotBlank private String email;

  @NotBlank @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") private String password;

  @NotBlank @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") private String firstName;

  @NotBlank @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters") private String lastName;
}
