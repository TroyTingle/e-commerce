package orderservice.clients;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ecommerce.product.v1.ProductResponse;
import ecommerce.product.v1.ProductServiceGrpc;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ttingle.commonlib.dto.ProductDto;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

  private static final UUID PRODUCT_ID = randomUUID();

  @Mock private ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

  @Test
  void whenGetProductByUuidCalled_thenReturnMappedProductDto() {
    ProductResponse productResponse =
        ProductResponse.newBuilder()
            .setId(PRODUCT_ID.toString())
            .setName("Keyboard")
            .setDescription("Mechanical Keyboard")
            .setPrice(12345)
            .setCurrency("GBP")
            .setSku("KEYBOARD-001")
            .setCategory("Accessories")
            .build();
    ProductServiceClient productServiceClient = new ProductServiceClient(productServiceStub);

    when(productServiceStub.getProductByUuid(
            argThat(request -> request.getProductId().equals(PRODUCT_ID.toString()))))
        .thenReturn(productResponse);

    ProductDto response = productServiceClient.getProductByUuid(PRODUCT_ID);

    assertThat(response.getId()).isEqualTo(PRODUCT_ID);
    assertThat(response.getName()).isEqualTo("Keyboard");
    assertThat(response.getDescription()).isEqualTo("Mechanical Keyboard");
    assertThat(response.getPrice()).isEqualByComparingTo("123.45");
    assertThat(response.getCurrency()).isEqualTo("GBP");
    assertThat(response.getSku()).isEqualTo("KEYBOARD-001");
    assertThat(response.getCategory()).isEqualTo("Accessories");
    verify(productServiceStub)
        .getProductByUuid(argThat(request -> request.getProductId().equals(PRODUCT_ID.toString())));
  }
}
