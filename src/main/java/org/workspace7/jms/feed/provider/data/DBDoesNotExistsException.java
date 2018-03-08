package org.workspace7.jms.feed.provider.data;

public class DBDoesNotExistsException extends Exception {
  private final String error;
  private final String reason;

  public DBDoesNotExistsException(String error, String reason) {
    super(error);
    this.error = error;
    this.reason = reason;
  }
}
