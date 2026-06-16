package uk.co.ttingle.inventoryservice.controllers;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryAvailabilityResponse;
import uk.co.ttingle.inventoryservice.generated.rest.v1.dto.InventoryItemResponse;
import uk.co.ttingle.inventoryservice.services.InventoryService;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

  private static final UUID PRODUCT_ID = randomUUID();

  @Mock private InventoryService inventoryService;

  @InjectMocks private InventoryController inventoryController;

  @Test
  void whenGetInventoryItemCalled_thenInventoryItemResponseReturned() {
    InventoryItemResponse inventoryItem = Instancio.of(InventoryItemResponse.class).create();

    when(inventoryService.getInventoryItem(PRODUCT_ID)).thenReturn(inventoryItem);

    ResponseEntity<InventoryItemResponse> response =
        inventoryController.getInventoryItem(PRODUCT_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(inventoryItem);
  }

  @Test
  void whenGetInventoryAvailabilityCalled_thenInventoryAvailabilityResponseReturned() {
    InventoryAvailabilityResponse availability =
        Instancio.of(InventoryAvailabilityResponse.class).create();

    when(inventoryService.getInventoryAvailability(PRODUCT_ID)).thenReturn(availability);

    ResponseEntity<InventoryAvailabilityResponse> response =
        inventoryController.getInventoryAvailability(PRODUCT_ID);

    assertThat(response.getStatusCode()).isEqualTo(OK);
    assertThat(response.getBody()).isEqualTo(availability);
  }
}
