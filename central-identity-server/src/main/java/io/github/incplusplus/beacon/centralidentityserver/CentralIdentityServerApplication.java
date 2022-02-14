package io.github.incplusplus.beacon.centralidentityserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(
    exclude = {
      // https://github.com/zalando/problem-spring-web/tree/main/problem-spring-web#configuration
      ErrorMvcAutoConfiguration.class
    })
public class CentralIdentityServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CentralIdentityServerApplication.class, args);
  }
}
