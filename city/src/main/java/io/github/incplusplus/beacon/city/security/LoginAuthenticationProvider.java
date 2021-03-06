package io.github.incplusplus.beacon.city.security;

import io.github.incplusplus.beacon.city.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.city.service.CisCommunicationsService;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class LoginAuthenticationProvider implements AuthenticationProvider {

  private static final Map<String, UserAccountDto> userInfoMap = new ConcurrentHashMap<>();
  private final AutoRegisterCity autoRegisterCity;
  private final CisCommunicationsService cisCommunicationsService;

  public LoginAuthenticationProvider(
      AutoRegisterCity autoRegisterCity, CisCommunicationsService cisCommunicationsService) {
    this.autoRegisterCity = autoRegisterCity;
    this.cisCommunicationsService = cisCommunicationsService;
  }

  private static List<GrantedAuthority> getAuthorities(List<String> roles) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority(role));
    }
    return authorities;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String name = authentication.getName();
    String password = authentication.getCredentials().toString();

    try {
      UserAccountDto account = cisCommunicationsService.verifyUser(name, password);
      // Cache the user's info for any endpoints that might need to know the user's ID from their
      // name
      userInfoMap.put(name, account);
      // Since we've gotten this far, the verify-user endpoint must have returned 200. Go ahead and
      // authorize the user.
      List<GrantedAuthority> grantedAuths = new ArrayList<>();
      grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
      return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);
    } catch (WebClientResponseException | WebClientRequestException e) {
      log.error(
          "The City encountered an exception when trying to verify a user with the CIS at '"
              + autoRegisterCity.getCISUrl()
              + "'.",
          e);
      /*
      Unfortunately, there isn't a better way to communicate to the user that the CIS is having problems except by returning null.
      This will still cause a 401 error, but it will at least have a message that will look suspicious and warrant investigation.
      It'll say "No AuthenticationProvider found for org.springframework.security.authentication.UsernamePasswordAuthenticationToken".
       */
      return null;
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

  public String getIdForUsername(String username) {
    return userInfoMap.get(username).getId();
  }
}
