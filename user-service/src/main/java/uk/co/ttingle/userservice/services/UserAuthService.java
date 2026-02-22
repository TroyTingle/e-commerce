package uk.co.ttingle.userservice.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uk.co.ttingle.userservice.exceptions.EmailConflictException;
import uk.co.ttingle.userservice.models.User;
import uk.co.ttingle.userservice.models.dto.AuthResponse;
import uk.co.ttingle.userservice.models.dto.LoginRequest;
import uk.co.ttingle.userservice.models.dto.RegisterRequest;
import uk.co.ttingle.userservice.models.dto.UserDto;
import uk.co.ttingle.userservice.repositories.UserRepository;
import uk.co.ttingle.commonlib.security.JwtUtil;

import static uk.co.ttingle.commonlib.security.JwtConstants.BEARER;
import static uk.co.ttingle.userservice.enums.Role.CUSTOMER;

@Service
@RequiredArgsConstructor
public class UserAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;


  @Transactional
  public UserDto registerUser(RegisterRequest registerRequest) {
    if (userRepository.existsByEmail(registerRequest.getEmail())) {
      throw new EmailConflictException("Email is already in use");
    }

    User newUser = userRepository.save(
        User.builder()
            .email(registerRequest.getEmail())
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .role(CUSTOMER)
            .build()
    );

    return UserDto.builder()
        .email(newUser.getEmail())
        .firstName(newUser.getFirstName())
        .lastName(newUser.getLastName())
        .build();
  }

  @Transactional
  public AuthResponse loginUser(LoginRequest loginRequest) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

    String token = jwtUtil.generateToken(loginRequest.getEmail());
    return AuthResponse.builder()
        .token(token)
        .type(BEARER)
        .build();
  }
}
