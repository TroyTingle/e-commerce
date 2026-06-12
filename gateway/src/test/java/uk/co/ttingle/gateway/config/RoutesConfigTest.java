package uk.co.ttingle.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.filter.factory.PreserveHostHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.support.GenericApplicationContext;

class RoutesConfigTest {

  @Test
  void declaresExpectedGatewayRoutes() {
    GenericApplicationContext context = new GenericApplicationContext();
    context.registerBean(
        PathRoutePredicateFactory.class,
        () -> new PathRoutePredicateFactory(new WebFluxProperties()));
    context.registerBean(
        PreserveHostHeaderGatewayFilterFactory.class, PreserveHostHeaderGatewayFilterFactory::new);
    context.refresh();

    RouteLocator locator =
        new RoutesConfig()
            .gatewayRoutes(
                new RouteLocatorBuilder(context),
                new GatewayProperties(
                    new GatewayProperties.Services("http://user", "http://product", "http://order"),
                    null));

    assertThat(locator.getRoutes().map(route -> route.getId()).collectList().block())
        .containsExactlyInAnyOrder(
            "user-auth", "user-profile", "product-catalog", "product-admin", "orders");
  }
}
