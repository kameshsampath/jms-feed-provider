package org.workspace7.jms.feed.provider.data;

public class DBAlreadyExistsException extends Exception {
  private final String error;
  private final String reason;

  public DBAlreadyExistsException(String error, String reason) {
    super(error);
    this.error = error;
    this.reason = reason;
  }
}
