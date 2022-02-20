package io.github.incplusplus.beacon.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A base class for both the CIS and City projects to extend. Using this base class helps avoid code
 * duplication and consequential fragmentation if changes are made in one copy but not the other.
 * Don't forget to annotate your subclass with the {@link Controller} annotation.
 */
public class SwaggerRedirectControllerBase {

  @GetMapping("/")
  public String redirectToSwaggerUI() {
    return "redirect:/swagger-ui/index.html";
  }
}
