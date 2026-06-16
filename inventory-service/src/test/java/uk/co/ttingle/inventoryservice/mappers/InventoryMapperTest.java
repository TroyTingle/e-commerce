package uk.co.ttingle.inventoryservice.mappers;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.co.ttingle.inventoryservice.enums.StockAdjustmentReason;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryAvailabilityResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentResponse;
import uk.co.ttingle.inventoryservice.models.InventoryItem;
import uk.co.ttingle.inventoryservice.models.StockAdjustment;

@Tag("unit")
class InventoryMapperTest {

  private static final UUID PRODUCT_ID = randomUUID();
  private static final String SKU = "TSHIRT-001";

  private final InventoryMapper inventoryMapper = new InventoryMapper();

  @Test
  void whenToInventoryItemResponseCalled_thenDerivedAvailableQuantityReturned() {
    InventoryItem inventoryItem = inventoryItem();

    InventoryItemResponse response = inventoryMapper.toInventoryItemResponse(inventoryItem);

    assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
    assertThat(response.getSku()).isEqualTo(SKU);
    assertThat(response.getQuantityOnHand()).isEqualTo(10);
    assertThat(response.getReservedQuantity()).isEqualTo(3);
    assertThat(response.getAvailableQuantity()).isEqualTo(7);
    assertThat(response.getUpdatedAt()).isNotNull();
  }

  @Test
  void whenToInventoryAvailabilityResponseCalled_thenSafeAvailabilityReturned() {
    InventoryItem inventoryItem = inventoryItem();

    InventoryAvailabilityResponse response =
        inventoryMapper.toInventoryAvailabilityResponse(inventoryItem);

    assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
    assertThat(response.getSku()).isEqualTo(SKU);
    assertThat(response.getAvailableQuantity()).isEqualTo(7);
    assertThat(response.getUpdatedAt()).isNotNull();
  }

  @Test
  void whenToStockAdjustmentResponseCalled_thenAuditFieldsReturned() {
    StockAdjustment adjustment =
        StockAdjustment.builder()
            .id(randomUUID())
            .productId(PRODUCT_ID)
            .sku(SKU)
            .quantityDelta(5)
            .reason(StockAdjustmentReason.RECEIVED)
            .reference("PO-1001")
            .createdBy(randomUUID().toString())
            .createdAt(now())
            .build();

    StockAdjustmentResponse response = inventoryMapper.toStockAdjustmentResponse(adjustment);

    assertThat(response.getId()).isEqualTo(adjustment.getId());
    assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
    assertThat(response.getSku()).isEqualTo(SKU);
    assertThat(response.getQuantityDelta()).isEqualTo(5);
    assertThat(response.getReason()).isEqualTo(StockAdjustmentReason.RECEIVED);
    assertThat(response.getReference()).isEqualTo("PO-1001");
    assertThat(response.getCreatedBy()).isEqualTo(adjustment.getCreatedBy());
    assertThat(response.getCreatedAt()).isNotNull();
  }

  private static InventoryItem inventoryItem() {
    return InventoryItem.builder()
        .productId(PRODUCT_ID)
        .sku(SKU)
        .quantityOnHand(10)
        .reservedQuantity(3)
        .createdAt(now())
        .updatedAt(now())
        .build();
  }
}
