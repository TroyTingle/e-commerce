package orderservice.integration;

import static java.util.UUID.randomUUID;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import orderservice.clients.ProductServiceClient;
import orderservice.enums.OrderStatus;
import orderservice.models.Order;
import orderservice.models.OrderItem;
import orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.co.ttingle.commonlib.dto.ProductDto;
import uk.co.ttingle.commonlib.security.JwtConstants;
import uk.co.ttingle.commonlib.security.JwtTokenUtil;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseIntegrationTest {

  protected static final String ORDER_URL = "/api/v1/orders";

  @LocalServerPort private int port;

  protected RestClient orderServiceClient;
  protected RestClient adminOrderServiceClient;
  protected RestClient unauthenticatedOrderServiceClient;

  protected final UUID userId = randomUUID();
  protected final UUID adminUserId = randomUUID();

  @Autowired protected OrderRepository orderRepository;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @MockitoBean protected ProductServiceClient productServiceClient;

  static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:18.3-alpine")
          .withDatabaseName("orderdb")
          .withUsername("test")
          .withPassword("test");

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeEach
  void setUpClient() {
    orderRepository.deleteAll();
    orderServiceClient = authenticatedClient(userId, "order-user@example.com", List.of("USER"));
    adminOrderServiceClient =
        authenticatedClient(adminUserId, "order-admin@example.com", List.of("ADMIN"));
    unauthenticatedOrderServiceClient =
        RestClient.builder().baseUrl("http://localhost:" + port).build();
  }

  protected ProductDto buildProduct(UUID productId, String name, BigDecimal price) {
    return ProductDto.builder()
        .id(productId)
        .name(name)
        .description(name + " description")
        .price(price)
        .currency("GBP")
        .sku(name.toUpperCase() + "-001")
        .category("Accessories")
        .build();
  }

  protected Order buildOrderForUser(UUID orderUserId, OrderStatus status) {
    Order order =
        Order.builder()
            .userId(orderUserId)
            .status(status)
            .totalAmount(BigDecimal.valueOf(19.99))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    OrderItem item =
        OrderItem.builder()
            .productId(randomUUID())
            .productNameAtPurchase("Keyboard")
            .quantity(1)
            .priceAtPurchase(BigDecimal.valueOf(19.99))
            .order(order)
            .build();
    order.setItems(List.of(item));
    return order;
  }

  private RestClient authenticatedClient(
      UUID authenticatedUserId, String email, List<String> roles) {
    String token = jwtTokenUtil.generateUserToken(authenticatedUserId, email, roles);
    return RestClient.builder()
        .baseUrl("http://localhost:" + port)
        .defaultHeader(JwtConstants.AUTH_HEADER, JwtConstants.BEARER_PREFIX + token)
        .build();
  }
}
