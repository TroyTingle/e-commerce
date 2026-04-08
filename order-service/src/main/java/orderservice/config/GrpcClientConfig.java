package orderservice.config;

import ecommerce.product.v1.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
