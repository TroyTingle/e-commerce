package uk.co.ttingle.inventoryservice.controllers;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentRequest;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.StockAdjustmentResponse;
import uk.co.ttingle.inventoryservice.services.InventoryService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminInventoryControllerTest {

  private static final UUID PRODUCT_ID = randomUUID();

  @Mock private InventoryService inventoryService;

  @InjectMocks private AdminInventoryController adminInventoryController;

  @Test
  void whenCreateStockAdjustmentCalled_thenInventoryItemResponseReturned() {
    StockAdjustmentRequest request = Instancio.of(StockAdjustmentRequest.class).create();
    InventoryItemResponse inventoryItem = Instancio.of(InventoryItemResponse.class).create();

    when(inventoryService.createStockAdjustment(PRODUCT_ID, request, null))
        .thenReturn(inventoryItem);

    ResponseEntity<InventoryItemResponse> response =
        adminInventoryController.createStockAdjustment(PRODUCT_ID, request);

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
    assertThat(response.getBody()).isEqualTo(inventoryItem);
  }

  @Test
  void whenGetStockAdjustmentsCalled_thenStockAdjustmentResponsesReturned() {
    List<StockAdjustmentResponse> adjustments =
        Instancio.ofList(StockAdjustmentResponse.class).size(2).create();

    when(inventoryService.getStockAdjustments(PRODUCT_ID)).thenReturn(adjustments);

    ResponseEntity<List<StockAdjustmentResponse>> response =
        adminInventoryController.getStockAdjustments(PRODUCT_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(adjustments);
  }
}
