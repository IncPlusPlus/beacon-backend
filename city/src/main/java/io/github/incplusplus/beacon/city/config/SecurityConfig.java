package io.github.incplusplus.beacon.city.config;

import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final SecurityProblemSupport problemSupport;
  private final AutoRegisterCity autoRegisterCity;

  @Autowired
  public SecurityConfig(SecurityProblemSupport problemSupport, AutoRegisterCity autoRegisterCity) {
    super();
    this.problemSupport = problemSupport;
    this.autoRegisterCity = autoRegisterCity;
  }

  @Override
  protected void configure(final AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authProvider());
  }

  @Override
  public void configure(final WebSecurity web) {
    web.ignoring()
        .antMatchers("/resources/**")
        // For the Swagger UI
        .antMatchers("/swagger-ui/**")
        // To allow the base path to redirect to the Swagger UI via SwaggerRedirectController
        .antMatchers("/")
        // For access to /v3/api-docs/swagger-config
        .antMatchers("/v3/api-docs/**")
        // For access to /v3/api-docs.yml
        .antMatchers("/v3/api-docs.yml")
        // For access to websocket endpoints (see
        // io.github.incplusplus.beacon.city.websocket.config.WebSocketConfig#registerStompEndpoints)
        .antMatchers("/beacon-ws/**");
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    // @formatter:off
    http.csrf().disable()
        .authorizeRequests()
          .antMatchers("/invalidSession*").anonymous()
          .anyRequest().authenticated()
          .and()
        .httpBasic().authenticationEntryPoint(problemSupport)
          .and()
        .exceptionHandling()
          .authenticationEntryPoint(problemSupport)
          .accessDeniedHandler(problemSupport);
    // @formatter:on
    // https://www.baeldung.com/spring-cors#cors-with-spring-security
    http.cors();
  }

  @Bean
  public LoginAuthenticationProvider authProvider() {
    return new LoginAuthenticationProvider(autoRegisterCity);
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder(11);
  }
}
