package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.InvitesApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.city.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.city.service.InviteService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class InviteController implements InvitesApi {
  private final InviteService inviteService;
  private final IAuthenticationFacade authenticationFacade;

  @Autowired
  public InviteController(InviteService inviteService, IAuthenticationFacade authenticationFacade) {
    this.inviteService = inviteService;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  public ResponseEntity<TowerInviteDto> createInvite(
      String towerId, Integer expiryTime, String expiryTimeUnit, Integer maxUses) {
    return new ResponseEntity<>(
        inviteService.createInvite(
            authenticationFacade.getAuthentication().getName(),
            towerId,
            expiryTime,
            expiryTimeUnit,
            maxUses),
        HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> joinUsingInvite(String towerInviteCode) {
    inviteService.joinTowerUsingInvite(
        authenticationFacade.getAuthentication().getName(), towerInviteCode);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<TowerInviteDto>> listInvites(String towerId) {
    return ResponseEntity.ok(inviteService.listInvitesForTower(towerId));
  }

  @Override
  public ResponseEntity<TowerInviteDto> revokeInvite(String towerId, String towerInviteCode) {
    return ResponseEntity.ok(inviteService.revokeInvite(towerId, towerInviteCode));
  }
}
