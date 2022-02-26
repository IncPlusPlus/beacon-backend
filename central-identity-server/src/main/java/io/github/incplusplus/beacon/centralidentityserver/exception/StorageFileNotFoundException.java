package io.github.incplusplus.beacon.centralidentityserver.exception;

import java.net.URI;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public class StorageFileNotFoundException extends AbstractThrowableProblem {
  private static final URI TYPE =
      URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404");

  public StorageFileNotFoundException(String message) {
    super(TYPE, "File not found", Status.NOT_FOUND, message);
  }

  public StorageFileNotFoundException(String message, Throwable cause) {
    super(TYPE, "File not found", Status.NOT_FOUND, message, null, (ThrowableProblem) cause);
  }
}
