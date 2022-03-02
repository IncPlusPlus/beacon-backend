package io.github.incplusplus.beacon.centralidentityserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final SecurityProblemSupport problemSupport;

  private final UserDetailsService userDetailsService;

  @Autowired
  public SecurityConfig(
      SecurityProblemSupport problemSupport, UserDetailsService userDetailsService) {
    super();
    this.problemSupport = problemSupport;
    this.userDetailsService = userDetailsService;
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
        .antMatchers("/v3/api-docs.yml");
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    // @formatter:off
    http.csrf().disable()
        .authorizeRequests()
          .antMatchers("/account/create-new-account").permitAll()
          .antMatchers("/city-cis-intercom/register-city").permitAll()
          .antMatchers("/invalidSession*").anonymous()
          .antMatchers("/city-cis-intercom/**").hasRole("CITY")
          .anyRequest().hasRole("USER")
          .and()
        .httpBasic().authenticationEntryPoint(problemSupport)
          .and()
        .exceptionHandling()
          .authenticationEntryPoint(problemSupport)
          .accessDeniedHandler(problemSupport);
    // @formatter:on
  }

  @Bean
  public DaoAuthenticationProvider authProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(encoder());
    return authProvider;
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder(11);
  }
}
