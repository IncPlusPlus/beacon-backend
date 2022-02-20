package io.github.incplusplus.beacon.centralidentityserver.service;

import io.github.incplusplus.beacon.centralidentityserver.config.SecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ComponentScan({"io.github.incplusplus.beacon.centralidentityserver.mapper"})
// https://stackoverflow.com/a/70047908/1687436
@TestPropertySource(properties = "spring.mongodb.embedded.version=4.0.2")
@ActiveProfiles("test")
public abstract class AbstractServiceTest {
  /** Effectively just {@link SecurityConfig#encoder()} */
  public PasswordEncoder encoder = new BCryptPasswordEncoder(11);
}
