package uk.co.ttingle.inventoryservice.exceptions;

public class InventoryItemNotFoundException extends RuntimeException {

  public InventoryItemNotFoundException(String message) {
    super(message);
  }
}
