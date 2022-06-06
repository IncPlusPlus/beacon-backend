package io.github.incplusplus.beacon.city;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("io.github.incplusplus.beacon.city.properties")
public class CityApplication {
  public static void main(String[] args) {
    SpringApplication.run(CityApplication.class, args);
  }
}
