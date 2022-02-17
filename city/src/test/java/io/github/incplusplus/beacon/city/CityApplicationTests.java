package io.github.incplusplus.beacon.city;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// https://stackoverflow.com/a/70047908/1687436
@TestPropertySource(properties = "spring.mongodb.embedded.version=4.0.2")
@SpringBootTest
class CityApplicationTests {

  @Test
  void contextLoads() {}
}
