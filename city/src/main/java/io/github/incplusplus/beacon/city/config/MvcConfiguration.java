package io.github.incplusplus.beacon.city.config;

import io.github.incplusplus.beacon.city.properties.LocalStorageProperties;
import io.github.incplusplus.beacon.common.config.MvcConfigurationBase;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class MvcConfiguration extends MvcConfigurationBase {
  private final Path uploadDir;

  public MvcConfiguration(@Autowired LocalStorageProperties properties) {
    this.uploadDir = Paths.get(properties.getLocation());
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    super.addResourceHandlers(registry);

    // Add resource handler for attachments
    registry
        .addResourceHandler("/attachments/**")
        .addResourceLocations("file:" + uploadDir.toAbsolutePath() + "/");
  }
}
