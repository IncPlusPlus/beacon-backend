package io.github.incplusplus.beacon.city.websocket.config;

import static org.springframework.security.web.authentication.www.BasicAuthenticationConverter.AUTHENTICATION_SCHEME_BASIC;

import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final LoginAuthenticationProvider authProvider;

  @Autowired
  public WebSocketConfig(LoginAuthenticationProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/beacon-ws")
        // https://stackoverflow.com/a/46870417/1687436
        .setAllowedOriginPatterns(
            "http://localhost:[*]",
            "https://beacon-frontend-pr-*.herokuapp.com",
            "https://beacon-frontend-main-staging.herokuapp.com",
            "https://beacon-frontend-prod.herokuapp.com")
        .withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-authentication-token-based
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
              UsernamePasswordAuthenticationToken credentials = convert(accessor);
              if (credentials == null) {
                throw new BadCredentialsException(
                    "Expected proper basic auth credentials in the websocket CONNECT message's \"Authorization\" header");
              }
              // Verify credentials with the CIS
              Authentication user = authProvider.authenticate(credentials);
              /*
              Allows for java.security.Principal to be retrieved as method argument
              in the websocket methods annotated with @Controller.
              See https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#supported-method-arguments
              and https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp-authentication for details.
              */
              accessor.setUser(user);
            }
            return message;
          }
        });
  }

  /**
   * This is pretty much a straight rip of the {@link
   * BasicAuthenticationConverter#convert(HttpServletRequest)} method except it takes a
   * StompHeaderAccessor instead of an HttpServletRequest.
   *
   * @param accessor a valid header accessor for an incoming CONNECT request
   * @return a UsernamePasswordAuthenticationToken for the provided credentials
   */
  private UsernamePasswordAuthenticationToken convert(StompHeaderAccessor accessor) {
    // Get the Authorization header that gets sent with the CONNECT message
    List<String> authHeader = accessor.getNativeHeader("Authorization");
    // If the header exists, this should be non-null. There should only be one value for
    // this header so the size should only be 1.
    if (authHeader == null || authHeader.size() != 1) {
      return null;
    }
    String authorizationHeaderValue = authHeader.get(0);
    if (authorizationHeaderValue == null) {
      return null;
    }
    authorizationHeaderValue = authorizationHeaderValue.trim();
    if (!StringUtils.startsWithIgnoreCase(authorizationHeaderValue, AUTHENTICATION_SCHEME_BASIC)) {
      return null;
    }
    if (authorizationHeaderValue.equalsIgnoreCase(AUTHENTICATION_SCHEME_BASIC)) {
      throw new BadCredentialsException("Empty basic authentication token");
    }
    byte[] base64Token = authorizationHeaderValue.substring(6).getBytes(StandardCharsets.UTF_8);
    byte[] decoded = decode(base64Token);
    String token = new String(decoded, StandardCharsets.UTF_8);
    int delim = token.indexOf(":");
    if (delim == -1) {
      throw new BadCredentialsException("Invalid basic authentication token");
    }
    UsernamePasswordAuthenticationToken result =
        new UsernamePasswordAuthenticationToken(
            token.substring(0, delim), token.substring(delim + 1));
    result.setDetails(accessor);
    return result;
  }

  private byte[] decode(byte[] base64Token) {
    try {
      return Base64.getDecoder().decode(base64Token);
    } catch (IllegalArgumentException ex) {
      throw new BadCredentialsException("Failed to decode basic authentication token");
    }
  }
}
