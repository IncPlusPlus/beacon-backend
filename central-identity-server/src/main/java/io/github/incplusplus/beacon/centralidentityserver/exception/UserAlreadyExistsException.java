package io.github.incplusplus.beacon.centralidentityserver.exception;

import java.net.URI;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public final class UserAlreadyExistsException extends AbstractThrowableProblem {

  private static final URI TYPE =
      URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/403");

  public UserAlreadyExistsException(String uniqueFieldName, String fieldValue) {
    super(
        TYPE,
        "Forbidden",
        Status.FORBIDDEN,
        "There is already an account with the " + uniqueFieldName + ": " + "'" + fieldValue + "'");
  }
}
