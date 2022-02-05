package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.SampleTextApi;
import io.github.incplusplus.beacon.city.generated.dto.HelloWorldResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController implements SampleTextApi {
  @Override
  public ResponseEntity<HelloWorldResponseDto> helloWorld() {
    return ResponseEntity.ok(new HelloWorldResponseDto().resultText("Hello, world!"));
  }
}
