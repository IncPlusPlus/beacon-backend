package io.github.incplusplus.beacon.centralidentityserver.config;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringdocConfig {
  @Bean
  SpringDocConfiguration springDocConfiguration() {
    return new SpringDocConfiguration();
  }

  @Bean
  public SpringDocConfigProperties springDocConfigProperties() {
    return new SpringDocConfigProperties();
  }
}
