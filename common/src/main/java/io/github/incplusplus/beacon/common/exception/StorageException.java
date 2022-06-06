package io.github.incplusplus.beacon.common.exception;

public class StorageException extends RuntimeException {

  public StorageException(String message) {
    super(message);
  }

  public StorageException(Throwable cause) {
    super(cause);
  }

  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
