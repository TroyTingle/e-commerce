package uk.co.ttingle.gateway.filters;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import uk.co.ttingle.commonlib.security.JwtConstants;
import uk.co.ttingle.commonlib.security.JwtTokenUtil;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

  private static final String USER_ID_HEADER = "X-User-Id";
  private static final String USER_EMAIL_HEADER = "X-User-Email";
  private static final String USER_ROLES_HEADER = "X-User-Roles";
  private static final List<String> PUBLIC_PREFIXES =
      List.of("/actuator/health", "/actuator/info", "/api/v1/auth/");

  private final JwtTokenUtil jwtTokenUtil;

  public JwtGatewayFilter(JwtTokenUtil jwtTokenUtil) {
    this.jwtTokenUtil = jwtTokenUtil;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().pathWithinApplication().value();

    if (isPublicRequest(exchange.getRequest())) {
      return chain.filter(stripUserContext(exchange));
    }

    String token = extractBearerToken(exchange.getRequest().getHeaders());
    if (token == null || !jwtTokenUtil.isTokenValid(token)) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    List<String> roles = jwtTokenUtil.extractRoles(token);
    if (path.startsWith("/api/v1/admin/") && !roles.contains("ADMIN")) {
      exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
      return exchange.getResponse().setComplete();
    }

    ServerHttpRequest request =
        exchange
            .getRequest()
            .mutate()
            .headers(
                headers -> {
                  headers.remove(USER_ID_HEADER);
                  headers.remove(USER_EMAIL_HEADER);
                  headers.remove(USER_ROLES_HEADER);
                })
            .header(USER_ID_HEADER, jwtTokenUtil.extractUserId(token).toString())
            .header(USER_EMAIL_HEADER, jwtTokenUtil.extractEmail(token))
            .header(USER_ROLES_HEADER, String.join(",", roles))
            .build();

    return chain.filter(exchange.mutate().request(request).build());
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }

  private boolean isPublicRequest(ServerHttpRequest request) {
    String path = request.getPath().pathWithinApplication().value();
    if (PUBLIC_PREFIXES.stream().anyMatch(path::startsWith)) {
      return true;
    }

    return ("GET".equals(request.getMethod().name()) || "HEAD".equals(request.getMethod().name()))
        && (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/categories"));
  }

  private ServerWebExchange stripUserContext(ServerWebExchange exchange) {
    ServerHttpRequest request =
        exchange
            .getRequest()
            .mutate()
            .headers(
                headers -> {
                  headers.remove(USER_ID_HEADER);
                  headers.remove(USER_EMAIL_HEADER);
                  headers.remove(USER_ROLES_HEADER);
                })
            .build();
    return exchange.mutate().request(request).build();
  }

  private String extractBearerToken(HttpHeaders headers) {
    String authHeader = headers.getFirst(JwtConstants.AUTH_HEADER);
    if (authHeader == null || !authHeader.startsWith(JwtConstants.BEARER_PREFIX)) {
      return null;
    }
    return authHeader.substring(JwtConstants.BEARER_PREFIX.length());
  }
}
