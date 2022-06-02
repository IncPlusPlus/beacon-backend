package io.github.incplusplus.beacon.city;

import io.github.incplusplus.beacon.city.properties.LocalStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({LocalStorageProperties.class})
public class CityApplication {
  public static void main(String[] args) {
    SpringApplication.run(CityApplication.class, args);
  }
}
