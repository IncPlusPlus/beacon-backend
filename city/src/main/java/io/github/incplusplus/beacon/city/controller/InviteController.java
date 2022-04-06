package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.InvitesApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerInviteDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class InviteController implements InvitesApi {

  @Override
  public ResponseEntity<TowerInviteDto> createInvite(
      String towerId, Integer expiryTime, String expiryTimeUnit, Integer maxUses) {
    // TODO: Implement creating invites (will require CIS communication in the service)
    return null;
  }

  @Override
  public ResponseEntity<Void> joinUsingInvite(String towerInviteCode) {
    // TODO: Implement joining the relevant Tower by using the invite code.
    //  Utilize towerInviteCode (verify it and use it with the CIS in the service layer).
    return null;
  }

  @Override
  public ResponseEntity<List<TowerInviteDto>> listInvites(String towerId) {
    // TODO: Implement listing invites (will require CIS communication in the service)
    return null;
  }

  @Override
  public ResponseEntity<TowerInviteDto> revokeInvite(String towerId, String towerInviteCode) {
    // TODO: Implement revoking invites (will require CIS communication in the service)
    return null;
  }
}
