package uk.co.ttingle.inventoryservice.integration;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import uk.co.ttingle.inventoryservice.enums.StockAdjustmentReason;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentRequest;

@Tag("integration")
class CreateStockAdjustmentIntegrationTest extends BaseIntegrationTest {

  @Test
  void whenCreateStockAdjustmentCalled_thenInventoryItemAndAdjustmentArePersisted() {
    UUID productId = randomUUID();
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .sku("TSHIRT-001")
            .quantityDelta(10)
            .reason(StockAdjustmentReason.RECEIVED)
            .reference("PO-1001")
            .build();

    Map<String, Object> inventoryItem =
        inventoryServiceClient
            .post()
            .uri("/api/v1/admin/inventory/{productId}/adjustments", productId)
            .body(request)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

    assertThat(inventoryItem)
        .containsEntry("productId", productId.toString())
        .containsEntry("sku", "TSHIRT-001")
        .containsEntry("quantityOnHand", 10)
        .containsEntry("reservedQuantity", 0)
        .containsEntry("availableQuantity", 10);

    List<Map<String, Object>> adjustments =
        inventoryServiceClient
            .get()
            .uri("/api/v1/admin/inventory/{productId}/adjustments", productId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

    assertThat(adjustments).hasSize(1);
    assertThat(adjustments.getFirst())
        .containsEntry("productId", productId.toString())
        .containsEntry("quantityDelta", 10)
        .containsEntry("reason", StockAdjustmentReason.RECEIVED.name());
  }

  @Test
  void whenCreateStockAdjustmentCalledWithoutAuthentication_thenUnauthorizedReturned() {
    UUID productId = randomUUID();

    HttpStatusCode status =
        anonymousInventoryServiceClient
            .post()
            .uri("/api/v1/admin/inventory/{productId}/adjustments", productId)
            .body(receivedStockRequest())
            .exchange((request, response) -> response.getStatusCode());

    assertThat(status.value()).isEqualTo(401);
  }

  @Test
  void whenCreateStockAdjustmentCalledWithoutAdminAuthority_thenForbiddenReturned() {
    UUID productId = randomUUID();

    HttpStatusCode status =
        customerInventoryServiceClient
            .post()
            .uri("/api/v1/admin/inventory/{productId}/adjustments", productId)
            .body(receivedStockRequest())
            .exchange((request, response) -> response.getStatusCode());

    assertThat(status.value()).isEqualTo(403);
  }

  @Test
  void whenCreateStockAdjustmentCalledWithMalformedJson_thenBadRequestReturned() {
    UUID productId = randomUUID();

    HttpStatusCode status =
        inventoryServiceClient
            .post()
            .uri("/api/v1/admin/inventory/{productId}/adjustments", productId)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{")
            .exchange((request, response) -> response.getStatusCode());

    assertThat(status.value()).isEqualTo(400);
  }

  @Test
  void whenCreateStockAdjustmentCalledWithInvalidReason_thenBadRequestReturned() {
    UUID productId = randomUUID();

    HttpStatusCode status =
        inventoryServiceClient
            .post()
            .uri("/api/v1/admin/inventory/{productId}/adjustments", productId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "sku": "TSHIRT-001",
                  "quantityDelta": 10,
                  "reason": "NOT_A_REASON"
                }
                """)
            .exchange((request, response) -> response.getStatusCode());

    assertThat(status.value()).isEqualTo(400);
  }

  private static StockAdjustmentRequest receivedStockRequest() {
    return StockAdjustmentRequest.builder()
        .sku("TSHIRT-001")
        .quantityDelta(10)
        .reason(StockAdjustmentReason.RECEIVED)
        .build();
  }
}
