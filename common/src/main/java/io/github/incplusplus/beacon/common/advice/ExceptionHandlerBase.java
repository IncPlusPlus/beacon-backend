package io.github.incplusplus.beacon.common.advice;

import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;

/**
 * A common superclass for problem-spring-web logic that will be used in both the City and CIS
 * projects. Be sure to use the {@link org.springframework.stereotype.Component} annotation on your
 * subclass.
 */
public class ExceptionHandlerBase implements ProblemHandling, SecurityAdviceTrait {
  @Override
  public boolean isCausalChainsEnabled() {
    return true;
  }
}
