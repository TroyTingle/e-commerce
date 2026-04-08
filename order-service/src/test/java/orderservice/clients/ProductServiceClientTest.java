package orderservice.clients;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecommerce.product.v1.ProductRequest;
import ecommerce.product.v1.ProductResponse;
import ecommerce.product.v1.ProductServiceGrpc;
import java.math.BigDecimal;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ttingle.commonlib.dto.ProductDto;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

  private static final UUID PRODUCT_ID = randomUUID();

  @Mock
  private ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  @InjectMocks
  private ProductServiceClient productServiceClient;

  @Test
  void whenGetProductByUuidCalled_thenMappedProductDtoReturned() {
    ProductDto expectedProduct =
        Instancio.of(ProductDto.class)
            .set(field(ProductDto::getId), PRODUCT_ID)
            .set(field(ProductDto::getPrice), new BigDecimal("12.34"))
            .create();
    ProductResponse grpcResponse =
        ProductResponse.newBuilder()
            .setId(expectedProduct.getId().toString())
            .setName(expectedProduct.getName())
            .setDescription(expectedProduct.getDescription())
            .setPrice(expectedProduct.getPrice().movePointRight(2).longValueExact())
            .setCurrency(expectedProduct.getCurrency())
            .setSku(expectedProduct.getSku())
            .setCategory(expectedProduct.getCategory())
            .build();

    when(productServiceStub.getProductByUuid(
            ProductRequest.newBuilder().setProductId(PRODUCT_ID.toString()).build()))
        .thenReturn(grpcResponse);

    ProductDto response = productServiceClient.getProductByUuid(PRODUCT_ID);

    ArgumentCaptor<ProductRequest> requestCaptor = ArgumentCaptor.forClass(ProductRequest.class);
    verify(productServiceStub).getProductByUuid(requestCaptor.capture());

    assertThat(requestCaptor.getValue().getProductId()).isEqualTo(PRODUCT_ID.toString());
    assertThat(response).usingRecursiveComparison().isEqualTo(expectedProduct);
  }
}
