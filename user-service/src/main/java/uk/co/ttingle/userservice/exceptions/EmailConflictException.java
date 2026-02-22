package uk.co.ttingle.userservice.exceptions;

public class EmailConflictException extends RuntimeException {
  public EmailConflictException(String message) {
    super(message);
  }
}
