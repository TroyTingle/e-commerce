package uk.co.ttingle.orderservice.clients;

import static java.util.UUID.fromString;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.co.ttingle.productservice.generated.grpc.v1.ProductRequest;
import uk.co.ttingle.productservice.generated.grpc.v1.ProductResponse;
import uk.co.ttingle.productservice.generated.grpc.v1.ProductServiceGrpc;
import uk.co.ttingle.productservice.generated.rest.v1.dto.ProductDto;

@Component
@RequiredArgsConstructor
public class ProductServiceClient {

  private final ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  public ProductDto getProductByUuid(UUID productId) {
    ProductResponse response =
        productServiceStub.getProductByUuid(
            ProductRequest.newBuilder().setProductId(productId.toString()).build());

    return ProductDto.builder()
        .id(fromString(response.getId()))
        .name(response.getName())
        .description(response.getDescription())
        .price(BigDecimal.valueOf(response.getPrice(), 2))
        .currency(response.getCurrency())
        .sku(response.getSku())
        .category(response.getCategory())
        .build();
  }
}
