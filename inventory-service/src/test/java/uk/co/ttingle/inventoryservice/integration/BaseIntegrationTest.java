package uk.co.ttingle.inventoryservice.integration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.co.ttingle.commonlib.security.JwtConstants;
import uk.co.ttingle.commonlib.security.JwtTokenUtil;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  protected RestClient inventoryServiceClient;
  protected RestClient anonymousInventoryServiceClient;
  protected RestClient customerInventoryServiceClient;

  @Container
  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:18.3-alpine")
          .withDatabaseName("inventorydb")
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
    anonymousInventoryServiceClient = RestClient.builder().baseUrl(baseUrl()).build();
    customerInventoryServiceClient = authenticatedClient("customer@example.com", "CUSTOMER");
    inventoryServiceClient = authenticatedClient("admin@example.com", "ADMIN");
  }

  private RestClient authenticatedClient(String email, String role) {
    String token =
        jwtTokenUtil.generateUserToken(java.util.UUID.randomUUID(), email, java.util.List.of(role));
    return RestClient.builder()
        .baseUrl(baseUrl())
        .defaultHeader(JwtConstants.AUTH_HEADER, JwtConstants.BEARER_PREFIX + token)
        .build();
  }

  private String baseUrl() {
    return "http://localhost:" + port;
  }
}
