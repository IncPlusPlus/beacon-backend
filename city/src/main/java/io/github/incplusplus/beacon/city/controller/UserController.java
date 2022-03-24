package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.UsersApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.service.TowerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class UserController implements UsersApi {
  private final TowerService towerService;

  @Autowired
  public UserController(TowerService towerService) {
    this.towerService = towerService;
  }

  @Override
  public ResponseEntity<String> createInvite(String towerId) {
    // TODO Implement
    return null;
  }

  @Override
  public ResponseEntity<List<TowerDto>> getUserTowerMemberships(String userAccountId) {
    return ResponseEntity.ok(towerService.getTowersUserIsMemberOf(userAccountId));
  }
}
