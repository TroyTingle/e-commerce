package uk.co.ttingle.gateway.filters;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import uk.co.ttingle.commonlib.security.JwtConstants;
import uk.co.ttingle.commonlib.security.JwtTokenUtil;

class JwtGatewayFilterTest {

  private JwtGatewayFilter filter;
  private JwtTokenUtil jwtTokenUtil;

  @BeforeEach
  void setUp() {
    jwtTokenUtil = new JwtTokenUtil();
    ReflectionTestUtils.setField(jwtTokenUtil, "secretKey", "your_very_secret_jwt_secret_key_32");
    ReflectionTestUtils.setField(jwtTokenUtil, "jwtExpirationMs", Duration.ofHours(1).toMillis());
    filter = new JwtGatewayFilter(jwtTokenUtil);
  }

  @Test
  void forwardsUserContextForAuthenticatedRequest() {
    UUID userId = UUID.randomUUID();
    String token = jwtTokenUtil.generateUserToken(userId, "user@example.com", List.of("CUSTOMER"));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.get(
                    "/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, JwtConstants.BEARER_PREFIX + token));
    CapturingChain chain = new CapturingChain();

    filter.filter(exchange, chain).block();

    assertThat(chain.exchange.getRequest().getHeaders().getFirst("X-User-Id"))
        .isEqualTo(userId.toString());
    assertThat(chain.exchange.getRequest().getHeaders().getFirst("X-User-Email"))
        .isEqualTo("user@example.com");
    assertThat(chain.exchange.getRequest().getHeaders().getFirst("X-User-Roles"))
        .isEqualTo("CUSTOMER");
  }

  @Test
  void rejectsProtectedRequestWithoutJwt() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.get(
                "/api/v1/orders"));

    filter.filter(exchange, ignored -> Mono.empty()).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void rejectsAdminRequestWithoutAdminRole() {
    String token =
        jwtTokenUtil.generateToken(
            UUID.randomUUID().toString(),
            Map.of(
                JwtConstants.EMAIL_CLAIM,
                "user@example.com",
                JwtConstants.ROLES_CLAIM,
                List.of("CUSTOMER")));
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.post(
                    "/api/v1/admin/products")
                .header(HttpHeaders.AUTHORIZATION, JwtConstants.BEARER_PREFIX + token));

    filter.filter(exchange, ignored -> Mono.empty()).block();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void allowsPublicCatalogReadWithoutJwt() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            org.springframework.mock.http.server.reactive.MockServerHttpRequest.get(
                "/api/v1/products"));
    CapturingChain chain = new CapturingChain();

    filter.filter(exchange, chain).block();

    assertThat(chain.exchange).isNotNull();
    assertThat(exchange.getResponse().getStatusCode()).isNull();
  }

  private static class CapturingChain
      implements org.springframework.cloud.gateway.filter.GatewayFilterChain {

    private ServerWebExchange exchange;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange) {
      this.exchange = exchange;
      return Mono.empty();
    }
  }
}
