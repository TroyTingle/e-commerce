package uk.co.ttingle.gateway.filters;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import uk.co.ttingle.gateway.config.GatewayProperties;

@Component
public class InMemoryRateLimitFilter implements GlobalFilter, Ordered {

  private final GatewayProperties.RateLimit rateLimit;
  private final Clock clock;
  private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();

  @Autowired
  public InMemoryRateLimitFilter(GatewayProperties properties) {
    this(properties.rateLimit(), Clock.systemUTC());
  }

  InMemoryRateLimitFilter(GatewayProperties.RateLimit rateLimit, Clock clock) {
    this.rateLimit = rateLimit;
    this.clock = clock;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String key = clientKey(exchange);
    long now = clock.millis();
    long windowMs = rateLimit.window().toMillis();

    RequestWindow window =
        windows.compute(
            key,
            (ignored, current) -> {
              if (current == null || now >= current.resetAtMillis()) {
                return new RequestWindow(now + windowMs, 1);
              }
              return new RequestWindow(current.resetAtMillis(), current.count() + 1);
            });

    if (window.count() > rateLimit.requests()) {
      exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
      return exchange.getResponse().setComplete();
    }

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE + 1;
  }

  private String clientKey(ServerWebExchange exchange) {
    String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    if (exchange.getRequest().getRemoteAddress() == null) {
      return "unknown";
    }
    return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
  }

  private record RequestWindow(long resetAtMillis, int count) {}
}
