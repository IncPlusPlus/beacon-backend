package io.github.incplusplus.beacon.centralidentityserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

  /**
   * The way the Swagger UI is rendered is by reading the OpenAPI specification YAML file. The way
   * this is typically read is by statically serving it. However, because
   * spring.web.resources.add-mappings is set to false, then the spec isn't automatically served for
   * the panel to access. To remedy this, a resource handler for the file has been added manually.
   *
   * @param registry the existing ResourceHandlerRegistry
   * @see <a href="https://www.baeldung.com/spring-mvc-static-resources">Serving Static
   *     Resources</a>
   * @see <a href="https://stackoverflow.com/a/64469835">a more manual way to do this</a>
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/v3/api-docs.yml")
        .addResourceLocations("classpath:/static")
        .resourceChain(true);
  }
}
