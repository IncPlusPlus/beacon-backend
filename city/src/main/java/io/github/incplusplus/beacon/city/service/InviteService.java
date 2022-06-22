package io.github.incplusplus.beacon.city.service;

import io.github.incplusplus.beacon.city.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.city.security.LoginAuthenticationProvider;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InviteService {
  private final LoginAuthenticationProvider loginAuthenticationProvider;
  private final CisCommunicationsService cisCommunicationsService;
  private final TowerService towerService;

  @Autowired
  public InviteService(
      LoginAuthenticationProvider loginAuthenticationProvider,
      CisCommunicationsService cisCommunicationsService,
      TowerService towerService) {
    this.loginAuthenticationProvider = loginAuthenticationProvider;
    this.cisCommunicationsService = cisCommunicationsService;
    this.towerService = towerService;
  }

  public TowerInviteDto createInvite(
      String inviterUsername,
      String towerId,
      Integer expiryTime,
      String expiryTimeUnit,
      Integer maxUses) {
    // TODO: We don't check that the inviter is a member of the tower they're making an invite for.
    Instant properExpiryTime = null;
    if (expiryTime > 0) {
      properExpiryTime = Instant.now().plus(expiryTime, ChronoUnit.valueOf(expiryTimeUnit));
    }
    // This is the info for the invite besides the ID
    TowerInviteDto inviteDto =
        new TowerInviteDto()
            .inviter(loginAuthenticationProvider.getIdForUsername(inviterUsername))
            .towerId(towerId)
            .expiryDate(properExpiryTime)
            .maxUses(maxUses);

    // With the desired details we've laid out, create the invite on the CIS
    return cisCommunicationsService.generateInvite(inviteDto);
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
    TowerInviteDto returnedInvite = cisCommunicationsService.useInvite(towerInviteCode);
    if (returnedInvite == null) {
      // TODO: Make a proper exception
      throw new RuntimeException("Couldn't retrieve info about this invite code from the CIS");
    }
    towerService.joinTower(username, returnedInvite.getTowerId());
  }

  public List<TowerInviteDto> listInvitesForTower(String towerId) {
    return cisCommunicationsService.getInvitesForTower(towerId);
  }

  public TowerInviteDto revokeInvite(String towerId, String towerInviteCode) {
    // TODO: Add error handling
    return cisCommunicationsService.revokeInvite(towerId, towerInviteCode);
  }
}
