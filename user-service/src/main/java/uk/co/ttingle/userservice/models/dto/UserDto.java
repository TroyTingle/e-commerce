package uk.co.ttingle.userservice.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {
  private String email;
  private String firstName;
  private String lastName;
}
