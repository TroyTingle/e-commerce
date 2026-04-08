package orderservice.integration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import ecommerce.product.v1.ProductServiceGrpc;
import java.util.Set;
import java.util.UUID;
import orderservice.integration.testdata.OrderData;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.co.ttingle.commonlib.security.JwtConstants;
import uk.co.ttingle.commonlib.security.JwtTokenUtil;

@Import({OrderData.class})
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @LocalServerPort private int port;

  @Autowired protected JwtTokenUtil jwtTokenUtil;

  @MockitoBean protected ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  protected RestClient orderServiceClient;

  @Container
  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:18.3-alpine")
          .withDatabaseName("orderdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeEach
  void setUpClient() {
    orderServiceClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
  }

  protected String bearerToken(UUID userId) {
    return JwtConstants.BEARER_PREFIX
        + jwtTokenUtil.generateUserToken(userId, "test@example.com", Set.of("ROLE_USER"));
  }

  protected HttpHeaders authHeaders(UUID userId) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, bearerToken(userId));
    return headers;
  }
}
