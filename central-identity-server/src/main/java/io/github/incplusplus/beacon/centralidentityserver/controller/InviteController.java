package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.InvitesApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.TowerInviteDto;
import io.github.incplusplus.beacon.centralidentityserver.service.TowerInviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class InviteController implements InvitesApi {
  private final TowerInviteService inviteService;

  @Autowired
  public InviteController(TowerInviteService inviteService) {
    this.inviteService = inviteService;
  }

  @Override
  public ResponseEntity<TowerInviteDto> getInviteInfo(String towerInviteCode) {
    return ResponseEntity.of(inviteService.getInvite(towerInviteCode));
  }
}
