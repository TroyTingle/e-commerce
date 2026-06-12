package uk.co.ttingle.orderservice.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.ttingle.productservice.generated.grpc.v1.ProductServiceGrpc;

@Configuration
public class GrpcClientConfig {

  @Bean(destroyMethod = "shutdown")
  public ManagedChannel productServiceChannel(
      @Value("${product-service.grpc.address}") String address) {
    return ManagedChannelBuilder.forTarget(address).usePlaintext().build();
  }

  @Bean
  public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub(
      ManagedChannel productServiceChannel) {
    return ProductServiceGrpc.newBlockingStub(productServiceChannel);
  }
}
