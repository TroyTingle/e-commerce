package productservice.grpc;

import ecommerce.product.v1.ProductRequest;
import ecommerce.product.v1.ProductResponse;
import ecommerce.product.v1.ProductServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.grpc.server.service.GrpcService;
import productservice.exceptions.ProductNotFoundException;
import productservice.services.ProductService;
import uk.co.ttingle.commonlib.dto.ProductDto;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {

  private final ProductService productService;

  @Override
  public void getProductByUuid(
      ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
    try {
      UUID productId = UUID.fromString(request.getProductId());
      ProductDto product = productService.getProductById(productId);
      ProductResponse response =
          ProductResponse.newBuilder()
              .setId(productId.toString())
              .setName(product.getName())
              .setDescription(product.getDescription())
              .setPrice(toMinorUnits(product.getPrice()))
              .setSku(product.getSku())
              .setCategory(product.getCategory())
              .setCurrency(product.getCurrency())
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (IllegalArgumentException ex) {
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid product id")
              .withCause(ex)
              .asRuntimeException());
    } catch (ProductNotFoundException ex) {
      responseObserver.onError(
          Status.NOT_FOUND.withDescription(ex.getMessage()).withCause(ex).asRuntimeException());
    } catch (Exception ex) {
      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Failed to resolve product price")
              .withCause(ex)
              .asRuntimeException());
    }
  }

  private static long toMinorUnits(BigDecimal amount) {
    return amount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
  }
}
