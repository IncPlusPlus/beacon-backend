package io.github.incplusplus.beacon.city.exception;

import java.net.URI;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

/** Serves as a flexible, basic exception for requests that should return a 400 status code. */
public class InvalidRequestException extends AbstractThrowableProblem {
  private static final URI TYPE =
      URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/400");

  public InvalidRequestException(String details) {
    super(TYPE, "Bad request", Status.BAD_REQUEST, details);
  }
}
