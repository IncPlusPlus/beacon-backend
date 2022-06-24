package io.github.incplusplus.beacon.common.exception;

import java.net.URI;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class EntityDoesNotExistException extends AbstractThrowableProblem {
  private static final URI TYPE =
      URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/404");

  public EntityDoesNotExistException(String entityType, String entityId) {
    super(
        TYPE,
        "Not found",
        Status.NOT_FOUND,
        "The " + entityType + " with ID '" + entityId + "' does not exist.");
  }
}
