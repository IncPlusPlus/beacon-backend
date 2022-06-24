package io.github.incplusplus.beacon.city.exception;

import java.net.URI;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class CisCommunicationsException extends AbstractThrowableProblem {
  private static final URI TYPE =
      URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/500");

  public CisCommunicationsException(String action, Exception error) {
    super(
        TYPE,
        "Internal Server Error",
        Status.INTERNAL_SERVER_ERROR,
        "There was an error when attempting to communicate with the Central Identity Server (CIS). This occurred when "
            + action
            + ". This is likely an error on our side. Try again later. The exception that caused this was "
            + error.toString());
  }
}
