package uk.co.ttingle.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfig {

  @Bean
  public RouteLocator gatewayRoutes(RouteLocatorBuilder routes, GatewayProperties properties) {
    GatewayProperties.Services services = properties.services();

    return routes
        .routes()
        .route(
            "user-auth",
            route ->
                route
                    .path("/api/v1/auth/**")
                    .filters(GatewayFilterSpec::preserveHostHeader)
                    .uri(services.userServiceUrl()))
        .route(
            "user-profile",
            route ->
                route
                    .path("/api/v1/users/**")
                    .filters(GatewayFilterSpec::preserveHostHeader)
                    .uri(services.userServiceUrl()))
        .route(
            "product-catalog",
            route ->
                route
                    .path("/api/v1/products/**", "/api/v1/categories/**")
                    .filters(GatewayFilterSpec::preserveHostHeader)
                    .uri(services.productServiceUrl()))
        .route(
            "product-admin",
            route ->
                route
                    .path("/api/v1/admin/products/**")
                    .filters(GatewayFilterSpec::preserveHostHeader)
                    .uri(services.productServiceUrl()))
        .route(
            "orders",
            route ->
                route
                    .path("/api/v1/orders/**")
                    .filters(GatewayFilterSpec::preserveHostHeader)
                    .uri(services.orderServiceUrl()))
        .build();
  }
}
