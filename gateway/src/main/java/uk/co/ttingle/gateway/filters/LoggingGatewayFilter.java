package uk.co.ttingle.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingGatewayFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    long startNanos = System.nanoTime();
    String method = exchange.getRequest().getMethod().name();
    String path = exchange.getRequest().getPath().pathWithinApplication().value();

    return chain
        .filter(exchange)
        .doFinally(
            _ -> {
              long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
              log.info(
                  "{} {} -> {} ({} ms)",
                  method,
                  path,
                  exchange.getResponse().getStatusCode(),
                  durationMs);
            });
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
