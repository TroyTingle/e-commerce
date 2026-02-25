package uk.co.ttingle.userservice.services;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.ttingle.commonlib.security.JwtUtil;
import uk.co.ttingle.userservice.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private UserAuthService userAuthService;



}
