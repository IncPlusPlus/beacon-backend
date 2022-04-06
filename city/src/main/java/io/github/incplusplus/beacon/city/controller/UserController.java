package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.UsersApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.city.service.TowerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class UserController implements UsersApi {
  private final TowerService towerService;
  private final IAuthenticationFacade authenticationFacade;

  @Autowired
  public UserController(TowerService towerService, IAuthenticationFacade authenticationFacade) {
    this.towerService = towerService;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  public ResponseEntity<List<TowerDto>> getUserTowerMemberships(String userAccountId) {
    // If no account ID was specified
    if (userAccountId == null || userAccountId.isBlank()) {
      // Provide the username of the user who sent this request and get the Towers they're in
      return ResponseEntity.ok(
          towerService.getTowersUserIsMemberOf(
              authenticationFacade.getAuthentication().getName(), true));
    }
    // Otherwise, get the Towers that the user specified by the provided ID is a member of
    return ResponseEntity.ok(towerService.getTowersUserIsMemberOf(userAccountId, false));
  }
}
