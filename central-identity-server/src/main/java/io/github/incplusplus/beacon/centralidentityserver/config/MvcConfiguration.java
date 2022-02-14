package io.github.incplusplus.beacon.centralidentityserver.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

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
        .resourceChain(true)
        .addResolver(
            new PathResourceResolver() {
              @Override
              protected Resource getResource(String resourcePath, Resource location)
                  throws IOException {
                Resource requestedResource = location.createRelative("/static" + resourcePath);
                if (requestedResource.exists() && requestedResource.isReadable()) {
                  return requestedResource;
                } else {
                  throw new IOException(
                      "Resource /static/v3/api-docs.yml not readable or doesn't exist");
                }
              }
            });
  }
}
