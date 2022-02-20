package io.github.incplusplus.beacon.common.config;

import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocConfiguration;
import org.springframework.context.annotation.Bean;

public class SpringdocConfigBase {
  @Bean
  SpringDocConfiguration springDocConfiguration() {
    return new SpringDocConfiguration();
  }

  @Bean
  public SpringDocConfigProperties springDocConfigProperties() {
    return new SpringDocConfigProperties();
  }
}
