package io.github.incplusplus.beacon.centralidentityserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// https://stackoverflow.com/a/70047908/1687436
@TestPropertySource(properties = "spring.mongodb.embedded.version=4.0.2")
@SpringBootTest
class CentralIdentityServerApplicationTests {

  @Test
  void contextLoads() {}
}
