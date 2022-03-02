package io.github.incplusplus.beacon.city.security;

import org.springframework.security.core.Authentication;

/**
 * This is the most reasonable way to retrieve user information inside a controller. Other methods
 * would require changing the controller method signature which isn't possible because they have to
 * override the OpenAPI generated code.
 *
 * @see <a href="https://www.baeldung.com/get-user-in-spring-security#interface">Baeldung</a>
 */
public interface IAuthenticationFacade {

  Authentication getAuthentication();
}
