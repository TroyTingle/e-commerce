package uk.co.ttingle.inventoryservice.exceptions;

public class InvalidStockAdjustmentException extends RuntimeException {

  public InvalidStockAdjustmentException(String message) {
    super(message);
  }
}
