package uk.co.ttingle.inventoryservice.services;

import static java.time.Instant.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import uk.co.ttingle.inventoryservice.enums.StockAdjustmentReason;
import uk.co.ttingle.inventoryservice.exceptions.InvalidStockAdjustmentException;
import uk.co.ttingle.inventoryservice.exceptions.InventoryItemNotFoundException;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentRequest;
import uk.co.ttingle.inventoryservice.mappers.InventoryMapper;
import uk.co.ttingle.inventoryservice.models.InventoryItem;
import uk.co.ttingle.inventoryservice.models.StockAdjustment;
import uk.co.ttingle.inventoryservice.repositories.InventoryItemRepository;
import uk.co.ttingle.inventoryservice.repositories.StockAdjustmentRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  private static final UUID PRODUCT_ID = randomUUID();
  private static final String SKU = "TSHIRT-001";

  @Mock private InventoryItemRepository inventoryItemRepository;

  @Mock private StockAdjustmentRepository stockAdjustmentRepository;

  private final InventoryMapper inventoryMapper = new InventoryMapper();

  private InventoryService inventoryService;

  @BeforeEach
  void setUp() {
    inventoryService =
        new InventoryService(inventoryItemRepository, stockAdjustmentRepository, inventoryMapper);
  }

  @Test
  void whenInventoryItemExists_thenInventoryItemResponseReturned() {
    InventoryItem inventoryItem = inventoryItem(10, 2);

    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(of(inventoryItem));

    InventoryItemResponse response = inventoryService.getInventoryItem(PRODUCT_ID);

    assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
    assertThat(response.getQuantityOnHand()).isEqualTo(10);
    assertThat(response.getReservedQuantity()).isEqualTo(2);
    assertThat(response.getAvailableQuantity()).isEqualTo(8);
  }

  @Test
  void whenInventoryItemDoesNotExist_thenNotFoundExceptionThrown() {
    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(empty());

    assertThatThrownBy(() -> inventoryService.getInventoryItem(PRODUCT_ID))
        .isInstanceOf(InventoryItemNotFoundException.class)
        .hasMessageContaining(PRODUCT_ID.toString());
  }

  @Test
  void whenAdjustmentCreatesNewInventoryItem_thenQuantityAndAuditRecordSaved() {
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .sku(SKU)
            .quantityDelta(5)
            .reason(StockAdjustmentReason.RECEIVED)
            .reference("PO-1001")
            .build();
    InventoryItem savedInventoryItem = inventoryItem(5, 0);

    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(empty());
    when(inventoryItemRepository.save(org.mockito.ArgumentMatchers.any(InventoryItem.class)))
        .thenReturn(savedInventoryItem);

    InventoryItemResponse response =
        inventoryService.createStockAdjustment(PRODUCT_ID, request, null);

    assertThat(response.getQuantityOnHand()).isEqualTo(5);
    assertThat(response.getAvailableQuantity()).isEqualTo(5);

    ArgumentCaptor<StockAdjustment> adjustmentCaptor = forClass(StockAdjustment.class);
    verify(stockAdjustmentRepository).save(adjustmentCaptor.capture());
    assertThat(adjustmentCaptor.getValue().getProductId()).isEqualTo(PRODUCT_ID);
    assertThat(adjustmentCaptor.getValue().getQuantityDelta()).isEqualTo(5);
    assertThat(adjustmentCaptor.getValue().getReason()).isEqualTo(StockAdjustmentReason.RECEIVED);
  }

  @Test
  void whenAdjustmentWouldMakeQuantityOnHandNegative_thenExceptionThrown() {
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .quantityDelta(-6)
            .reason(StockAdjustmentReason.CORRECTION)
            .build();

    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(of(inventoryItem(5, 0)));

    assertThatThrownBy(() -> inventoryService.createStockAdjustment(PRODUCT_ID, request, null))
        .isInstanceOf(InvalidStockAdjustmentException.class)
        .hasMessage("quantityOnHand cannot become negative");

    verify(inventoryItemRepository, never()).save(org.mockito.ArgumentMatchers.any());
    verify(stockAdjustmentRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void whenAdjustmentWouldMakeQuantityOnHandLowerThanReserved_thenExceptionThrown() {
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .quantityDelta(-3)
            .reason(StockAdjustmentReason.CORRECTION)
            .build();

    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(of(inventoryItem(5, 3)));

    assertThatThrownBy(() -> inventoryService.createStockAdjustment(PRODUCT_ID, request, null))
        .isInstanceOf(InvalidStockAdjustmentException.class)
        .hasMessage("quantityOnHand cannot be lower than reservedQuantity");

    verify(inventoryItemRepository, never()).save(org.mockito.ArgumentMatchers.any());
    verify(stockAdjustmentRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void whenQuantityDeltaIsZero_thenExceptionThrown() {
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .quantityDelta(0)
            .reason(StockAdjustmentReason.CORRECTION)
            .build();

    assertThatThrownBy(() -> inventoryService.createStockAdjustment(PRODUCT_ID, request, null))
        .isInstanceOf(InvalidStockAdjustmentException.class)
        .hasMessage("quantityDelta cannot be zero");

    verify(inventoryItemRepository, never()).findByProductId(PRODUCT_ID);
  }

  @Test
  void whenAuthenticatedAdjustmentApplied_thenCreatedByIsCaptured() {
    UUID adminId = randomUUID();
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .sku("TSHIRT-RENAMED")
            .quantityDelta(2)
            .reason(StockAdjustmentReason.RECEIVED)
            .build();
    InventoryItem savedInventoryItem =
        inventoryItem(7, 0).toBuilder().sku("TSHIRT-RENAMED").build();

    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(of(inventoryItem(5, 0)));
    when(inventoryItemRepository.save(org.mockito.ArgumentMatchers.any(InventoryItem.class)))
        .thenReturn(savedInventoryItem);

    inventoryService.createStockAdjustment(
        PRODUCT_ID, request, new UsernamePasswordAuthenticationToken(adminId, null));

    ArgumentCaptor<StockAdjustment> adjustmentCaptor = forClass(StockAdjustment.class);
    verify(stockAdjustmentRepository).save(adjustmentCaptor.capture());
    assertThat(adjustmentCaptor.getValue().getCreatedBy()).isEqualTo(adminId.toString());
    assertThat(adjustmentCaptor.getValue().getSku()).isEqualTo("TSHIRT-RENAMED");
  }

  @Test
  void whenNewInventoryAdjustmentHasNoSku_thenExceptionThrown() {
    StockAdjustmentRequest request =
        StockAdjustmentRequest.builder()
            .quantityDelta(5)
            .reason(StockAdjustmentReason.RECEIVED)
            .build();

    when(inventoryItemRepository.findByProductId(PRODUCT_ID)).thenReturn(empty());

    assertThatThrownBy(() -> inventoryService.createStockAdjustment(PRODUCT_ID, request, null))
        .isInstanceOf(InvalidStockAdjustmentException.class)
        .hasMessage("sku is required when creating inventory for a new product");
  }

  private static InventoryItem inventoryItem(int quantityOnHand, int reservedQuantity) {
    return InventoryItem.builder()
        .productId(PRODUCT_ID)
        .sku(SKU)
        .quantityOnHand(quantityOnHand)
        .reservedQuantity(reservedQuantity)
        .createdAt(now())
        .updatedAt(now())
        .build();
  }
}
