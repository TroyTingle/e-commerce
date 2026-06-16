package uk.co.ttingle.inventoryservice.controllers;

import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.inventoryservice.generated.rest.v1.AdminInventoryApiV1;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentRequest;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentResponse;
import uk.co.ttingle.inventoryservice.services.InventoryService;

@RestController
@RequiredArgsConstructor
public class AdminInventoryController implements AdminInventoryApiV1 {

  private final InventoryService inventoryService;

  @Override
  public ResponseEntity<InventoryItemResponse> createStockAdjustment(
      UUID productId, StockAdjustmentRequest stockAdjustmentRequest) {
    return ResponseEntity.status(CREATED)
        .body(
            inventoryService.createStockAdjustment(
                productId,
                stockAdjustmentRequest,
                SecurityContextHolder.getContext().getAuthentication()));
  }

  @Override
  public ResponseEntity<List<StockAdjustmentResponse>> getStockAdjustments(UUID productId) {
    return ResponseEntity.ok(inventoryService.getStockAdjustments(productId));
  }
}
