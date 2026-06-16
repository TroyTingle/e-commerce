package uk.co.ttingle.inventoryservice.controllers;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ttingle.inventoryservice.generated.rest.v1.InventoryApiV1;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryAvailabilityResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.services.InventoryService;

@RestController
@RequiredArgsConstructor
public class InventoryController implements InventoryApiV1 {

  private final InventoryService inventoryService;

  @Override
  public ResponseEntity<InventoryAvailabilityResponse> getInventoryAvailability(UUID productId) {
    return ResponseEntity.ok(inventoryService.getInventoryAvailability(productId));
  }

  @Override
  public ResponseEntity<InventoryItemResponse> getInventoryItem(UUID productId) {
    return ResponseEntity.ok(inventoryService.getInventoryItem(productId));
  }
}
