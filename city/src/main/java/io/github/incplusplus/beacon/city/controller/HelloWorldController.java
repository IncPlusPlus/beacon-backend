package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.SampleTextApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController implements SampleTextApi {
  @Override
  public ResponseEntity<Void> helloWorld() {
    return ResponseEntity.ok(null);
  }
}
