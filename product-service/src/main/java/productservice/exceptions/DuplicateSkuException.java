package productservice.exceptions;

public class DuplicateSkuException extends RuntimeException {
  public DuplicateSkuException(String message) {
    super(message);
  }
}
