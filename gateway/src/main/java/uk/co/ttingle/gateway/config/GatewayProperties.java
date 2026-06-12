package uk.co.ttingle.gateway.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public record GatewayProperties(Services services, RateLimit rateLimit) {

  public GatewayProperties {
    services =
        services == null
            ? new Services(
                "http://localhost:8081", "http://localhost:8082", "http://localhost:8083")
            : services;
    rateLimit = rateLimit == null ? new RateLimit(120, Duration.ofMinutes(1)) : rateLimit;
  }

  public record Services(String userServiceUrl, String productServiceUrl, String orderServiceUrl) {}

  public record RateLimit(int requests, Duration window) {}
}
