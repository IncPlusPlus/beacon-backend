package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.UsersApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class UserController implements UsersApi {

  @Override
  public ResponseEntity<String> createInvite(String towerId) {
    // TODO Implement
    return null;
  }

  @Override
  public ResponseEntity<List<TowerDto>> getUserTowerMemberships(String userAccountId) {
    // TODO Implement
    return null;
  }
}
