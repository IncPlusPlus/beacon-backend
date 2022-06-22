package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.city.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The purpose of this class is to interact with the CIS. This was previously done manually inside
 * the service classes. However, for the purpose of simplifying the business logic and making tests
 * easier to write, it made more sense to put these methods into a service class.
 */
@Service
public class CisCommunicationsService {
  private static final String INTERCOM_BASE = "/city-cis-intercom";
  private static final String CIS_INVITES_ENDPOINT = INTERCOM_BASE + "/invites";
  public static final String CIS_REGISTER_CITY_ENDPOINT = INTERCOM_BASE + "/register-city";
  private static final String CIS_VERIFY_USER_ENDPOINT = INTERCOM_BASE + "/verify-user";
  private static final String CIS_MEMBERSHIPS_ENDPOINT = INTERCOM_BASE + "/memberships";
  private final AutoRegisterCity autoRegisterCity;
  WebClient cisWebClient;

  @Autowired
  public CisCommunicationsService(AutoRegisterCity autoRegisterCity) {
    this.autoRegisterCity = autoRegisterCity;
  }

  // region CIS API Methods
  // region Invites
  public TowerInviteDto generateInvite(TowerInviteDto inviteDto) {
    // TODO: Add error handling to properly return 400 errors
    return getCisWebClient()
        .post()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(CIS_INVITES_ENDPOINT)
        .bodyValue(inviteDto)
        .retrieve()
        .bodyToMono(TowerInviteDto.class)
        .block(Duration.ofSeconds(30));
  }

  public List<TowerInviteDto> getInvitesForTower(String towerId) {
    // TODO: Add error handling
    return getCisWebClient()
        .get()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(
            uriBuilder ->
                uriBuilder.path(CIS_INVITES_ENDPOINT).queryParam("tower-id", towerId).build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<TowerInviteDto>>() {})
        .block(Duration.ofSeconds(30));
  }

  public TowerInviteDto useInvite(String towerInviteCode) {
    return getCisWebClient()
        .put()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(CIS_INVITES_ENDPOINT + "/" + towerInviteCode)
        .retrieve()
        .bodyToMono(TowerInviteDto.class)
        .block(Duration.ofSeconds(30));
  }

  public TowerInviteDto revokeInvite(String towerId, String towerInviteCode) {
    return getCisWebClient()
        .delete()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(CIS_INVITES_ENDPOINT + "/" + towerInviteCode)
                    // Unused
                    .queryParam("tower-id", towerId)
                    .build())
        .retrieve()
        .bodyToMono(TowerInviteDto.class)
        .block(Duration.ofSeconds(30));
  }
  // endregion

  public UserAccountDto verifyUser(String name, String password) {
    return getCisWebClient()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(CIS_VERIFY_USER_ENDPOINT)
                    .queryParam("username", name)
                    .queryParam("password", password)
                    .build())
        .retrieve()
        .onStatus(
            httpStatus -> httpStatus.equals(HttpStatus.UNAUTHORIZED),
            clientResponse -> {
              throw new BadCredentialsException(
                  "Invalid user credentials for account '" + name + "'.");
            })
        .onStatus(
            httpStatus -> httpStatus.equals(HttpStatus.NOT_FOUND),
            clientResponse -> {
              throw new UsernameNotFoundException("Username '" + name + "' not found.");
            })
        .bodyToMono(UserAccountDto.class)
        .block(Duration.ofSeconds(30));
  }

  // region City Members
  public List<String> addCityMembers(String userId) {
    return getCisWebClient()
        .put()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(CIS_MEMBERSHIPS_ENDPOINT)
        .bodyValue(List.of(userId))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
        .block(Duration.ofSeconds(30));
  }

  public List<String> removeCityMembers(List<String> userIds) {
    return getCisWebClient()
        .method(HttpMethod.DELETE)
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(CIS_MEMBERSHIPS_ENDPOINT)
        .bodyValue(userIds)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
        .block(Duration.ofSeconds(30));
  }
  // endregion
  // endregion

  /**
   * @return the WebClient for interacting with the CIS. This isn't put into a field in the
   *     constructor because the City ID and password aren't initialized at start and the
   *     setBasicAuth method will throw an exception if either of its parameters are null.
   */
  private WebClient getCisWebClient() {
    if (this.cisWebClient == null) {
      this.cisWebClient =
          WebClient.builder()
              .baseUrl(autoRegisterCity.getCISUrl())
              .defaultHeaders(
                  header ->
                      header.setBasicAuth(
                          autoRegisterCity.getCityId(), autoRegisterCity.getCityCISPassword()))
              .build();
    }
    return this.cisWebClient;
  }
}
