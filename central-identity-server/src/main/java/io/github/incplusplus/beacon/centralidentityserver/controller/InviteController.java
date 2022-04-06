package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.InvitesApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.TowerInviteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class InviteController implements InvitesApi {

  @Override
  public ResponseEntity<TowerInviteDto> getInviteInfo(String towerInviteCode) {
    // TODO: Implement getting invite info
    return null;
  }
}
