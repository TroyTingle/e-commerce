package uk.co.ttingle.inventoryservice.mappers;

import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneOffset.UTC;

import org.springframework.stereotype.Component;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryAvailabilityResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentResponse;
import uk.co.ttingle.inventoryservice.models.InventoryItem;
import uk.co.ttingle.inventoryservice.models.StockAdjustment;

@Component
public class InventoryMapper {

  public InventoryItemResponse toInventoryItemResponse(InventoryItem inventoryItem) {
    return InventoryItemResponse.builder()
        .productId(inventoryItem.getProductId())
        .sku(inventoryItem.getSku())
        .quantityOnHand(inventoryItem.getQuantityOnHand())
        .reservedQuantity(inventoryItem.getReservedQuantity())
        .availableQuantity(inventoryItem.getAvailableQuantity())
        .updatedAt(ofInstant(inventoryItem.getUpdatedAt(), UTC))
        .build();
  }

  public InventoryAvailabilityResponse toInventoryAvailabilityResponse(
      InventoryItem inventoryItem) {
    return InventoryAvailabilityResponse.builder()
        .productId(inventoryItem.getProductId())
        .sku(inventoryItem.getSku())
        .availableQuantity(inventoryItem.getAvailableQuantity())
        .updatedAt(ofInstant(inventoryItem.getUpdatedAt(), UTC))
        .build();
  }

  public StockAdjustmentResponse toStockAdjustmentResponse(StockAdjustment stockAdjustment) {
    return StockAdjustmentResponse.builder()
        .id(stockAdjustment.getId())
        .productId(stockAdjustment.getProductId())
        .sku(stockAdjustment.getSku())
        .quantityDelta(stockAdjustment.getQuantityDelta())
        .reason(stockAdjustment.getReason())
        .reference(stockAdjustment.getReference())
        .createdBy(stockAdjustment.getCreatedBy())
        .createdAt(ofInstant(stockAdjustment.getCreatedAt(), UTC))
        .build();
  }
}
