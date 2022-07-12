package io.github.incplusplus.beacon.city.config;

import io.github.incplusplus.beacon.city.properties.LocalStorageProperties;
import io.github.incplusplus.beacon.city.service.storage.LocalStorageImpl;
import io.github.incplusplus.beacon.common.config.MvcConfigurationBase;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
@ConditionalOnBean(value = LocalStorageImpl.class)
public class LocalStorageResourceHandlerConfig extends MvcConfigurationBase {
  private final Path uploadDir;

  public LocalStorageResourceHandlerConfig(@Autowired LocalStorageProperties properties) {
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
