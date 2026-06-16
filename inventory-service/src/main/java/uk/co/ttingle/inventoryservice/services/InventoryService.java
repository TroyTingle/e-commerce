package uk.co.ttingle.inventoryservice.services;

import static java.lang.String.format;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ttingle.inventoryservice.exceptions.InvalidStockAdjustmentException;
import uk.co.ttingle.inventoryservice.exceptions.InventoryItemNotFoundException;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryAvailabilityResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentRequest;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentResponse;
import uk.co.ttingle.inventoryservice.mappers.InventoryMapper;
import uk.co.ttingle.inventoryservice.models.InventoryItem;
import uk.co.ttingle.inventoryservice.models.StockAdjustment;
import uk.co.ttingle.inventoryservice.repositories.InventoryItemRepository;
import uk.co.ttingle.inventoryservice.repositories.StockAdjustmentRepository;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final InventoryItemRepository inventoryItemRepository;
  private final StockAdjustmentRepository stockAdjustmentRepository;
  private final InventoryMapper inventoryMapper;

  @Transactional(readOnly = true)
  public InventoryItemResponse getInventoryItem(UUID productId) {
    return inventoryMapper.toInventoryItemResponse(findInventoryItemByProductIdOrThrow(productId));
  }

  @Transactional(readOnly = true)
  public InventoryAvailabilityResponse getInventoryAvailability(UUID productId) {
    return inventoryMapper.toInventoryAvailabilityResponse(
        findInventoryItemByProductIdOrThrow(productId));
  }

  @Transactional
  public InventoryItemResponse createStockAdjustment(
      UUID productId, StockAdjustmentRequest request, Authentication authentication) {
    validateAdjustmentRequest(productId, request);

    InventoryItem inventoryItem =
        inventoryItemRepository
            .findByProductId(productId)
            .orElseGet(() -> newInventoryItem(productId, request));

    if (request.getSku() != null && !request.getSku().isBlank()) {
      inventoryItem.setSku(request.getSku());
    }

    int adjustedQuantityOnHand = inventoryItem.getQuantityOnHand() + request.getQuantityDelta();
    validateAdjustedQuantities(inventoryItem, adjustedQuantityOnHand);

    inventoryItem.setQuantityOnHand(adjustedQuantityOnHand);
    InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

    stockAdjustmentRepository.save(
        StockAdjustment.builder()
            .productId(productId)
            .sku(savedInventoryItem.getSku())
            .quantityDelta(request.getQuantityDelta())
            .reason(request.getReason())
            .reference(request.getReference())
            .createdBy(resolveActor(authentication))
            .build());

    return inventoryMapper.toInventoryItemResponse(savedInventoryItem);
  }

  @Transactional(readOnly = true)
  public List<StockAdjustmentResponse> getStockAdjustments(UUID productId) {
    return stockAdjustmentRepository.findAllByProductIdOrderByCreatedAtDesc(productId).stream()
        .map(inventoryMapper::toStockAdjustmentResponse)
        .toList();
  }

  private InventoryItem findInventoryItemByProductIdOrThrow(UUID productId) {
    return inventoryItemRepository
        .findByProductId(productId)
        .orElseThrow(
            () ->
                new InventoryItemNotFoundException(
                    format("No inventory item found with product id %s", productId)));
  }

  private static InventoryItem newInventoryItem(UUID productId, StockAdjustmentRequest request) {
    String sku = request.getSku();
    if (sku == null || sku.isBlank()) {
      throw new InvalidStockAdjustmentException(
          "sku is required when creating inventory for a new product");
    }

    return InventoryItem.builder()
        .productId(productId)
        .sku(sku)
        .quantityOnHand(0)
        .reservedQuantity(0)
        .build();
  }

  private static void validateAdjustmentRequest(UUID productId, StockAdjustmentRequest request) {
    if (productId == null) {
      throw new InvalidStockAdjustmentException("productId is required");
    }

    if (request.getQuantityDelta() == 0) {
      throw new InvalidStockAdjustmentException("quantityDelta cannot be zero");
    }
  }

  private static void validateAdjustedQuantities(
      InventoryItem inventoryItem, int adjustedQuantityOnHand) {
    if (adjustedQuantityOnHand < 0) {
      throw new InvalidStockAdjustmentException("quantityOnHand cannot become negative");
    }

    if (adjustedQuantityOnHand < inventoryItem.getReservedQuantity()) {
      throw new InvalidStockAdjustmentException(
          "quantityOnHand cannot be lower than reservedQuantity");
    }
  }

  private static String resolveActor(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      return null;
    }
    return authentication.getPrincipal().toString();
  }
}
