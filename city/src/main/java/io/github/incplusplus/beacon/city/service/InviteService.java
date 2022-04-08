package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import io.github.incplusplus.beacon.city.spring.AutoRegisterCity;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class InviteService {
  private static final String CIS_INVITES_ENDPOINT_URI = "/city-cis-intercom/invites";
  private final LoginAuthenticationProvider loginAuthenticationProvider;
  private final AutoRegisterCity autoRegisterCity;
  private final TowerService towerService;
  private WebClient cisWebClient;

  @Autowired
  public InviteService(
      LoginAuthenticationProvider loginAuthenticationProvider,
      AutoRegisterCity autoRegisterCity,
      TowerService towerService) {
    this.loginAuthenticationProvider = loginAuthenticationProvider;
    this.autoRegisterCity = autoRegisterCity;
    this.towerService = towerService;
  }

  public TowerInviteDto createInvite(
      String inviterUsername,
      String towerId,
      Integer expiryTime,
      String expiryTimeUnit,
      Integer maxUses) {
    Instant properExpiryTime = null;
    if (expiryTime > 0) {
      properExpiryTime = Instant.now().plus(expiryTime, ChronoUnit.valueOf(expiryTimeUnit));
    }
    TowerInviteDto inviteDto =
        new TowerInviteDto()
            .inviter(loginAuthenticationProvider.getIdForUsername(inviterUsername))
            .towerId(towerId)
            .expiryDate(properExpiryTime)
            .maxUses(maxUses);

    // TODO: Add error handling to properly return 400 errors
    return getCisWebClient()
        .post()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(CIS_INVITES_ENDPOINT_URI)
        .bodyValue(inviteDto)
        .retrieve()
        .bodyToMono(TowerInviteDto.class)
        .block(Duration.ofSeconds(30));
  }

  /**
   * This method will retrieve info about this invite from the CIS, tell the CIS to increment the
   * number of uses this invite has by one, and then add the user as a member of the tower the
   * invite refers to.
   *
   * @param username the username of the user who is attempting to use the invite code
   * @param towerInviteCode the invite code
   */
  public void joinTowerUsingInvite(String username, String towerInviteCode) {
    /*
    TODO: This doesn't check if the user is already a member and just immediately increments the
     number of uses the invite has. This isn't really a huge issue right now but could be remedied
     at a later time.
     */

    // Tell the CIS to increment the number of uses of the invite code + retrieve info about it
    // TODO: Add error handling
    TowerInviteDto returnedInvite =
        getCisWebClient()
            .put()
            //            .contentType(MediaType.APPLICATION_JSON)
            .uri(CIS_INVITES_ENDPOINT_URI + "/" + towerInviteCode)
            .retrieve()
            .bodyToMono(TowerInviteDto.class)
            .block(Duration.ofSeconds(30));
    if (returnedInvite == null) {
      // TODO: Make a proper exception
      throw new RuntimeException("Couldn't retrieve info about this invite code from the CIS");
    }
    towerService.joinTower(username, returnedInvite.getTowerId());
  }

  public List<TowerInviteDto> listInvitesForTower(String towerId) {
    // TODO: Add error handling
    return getCisWebClient()
        .get()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(
            uriBuilder ->
                uriBuilder.path(CIS_INVITES_ENDPOINT_URI).queryParam("tower-id", towerId).build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<TowerInviteDto>>() {})
        .block(Duration.ofSeconds(30));
  }

  public TowerInviteDto revokeInvite(String towerId, String towerInviteCode) {
    // TODO: Add error handling
    return getCisWebClient()
        .delete()
        //            .contentType(MediaType.APPLICATION_JSON)
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(CIS_INVITES_ENDPOINT_URI + "/" + towerInviteCode)
                    // Unused
                    .queryParam("tower-id", towerId)
                    .build())
        .retrieve()
        .bodyToMono(TowerInviteDto.class)
        .block(Duration.ofSeconds(30));
  }

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
