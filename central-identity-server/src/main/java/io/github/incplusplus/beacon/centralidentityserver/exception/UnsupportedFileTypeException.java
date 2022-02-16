package io.github.incplusplus.beacon.centralidentityserver.exception;

import java.net.URI;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class UnsupportedFileTypeException extends AbstractThrowableProblem {
  private static final URI TYPE =
      URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/400");

  public UnsupportedFileTypeException(String fileType, String supportedTypes) {
    super(
        TYPE,
        "Bad request",
        Status.BAD_REQUEST,
        "The file type '"+fileType+"' is not supported. Only files of type(s) '"+supportedTypes+"' are supported here.");
  }
}
