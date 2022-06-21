package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.TowersApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.city.service.TowerService;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class TowerController implements TowersApi {

  private final TowerService towerService;
  private final IAuthenticationFacade authenticationFacade;

  @Autowired
  public TowerController(TowerService towerService, IAuthenticationFacade authenticationFacade) {
    this.towerService = towerService;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  public ResponseEntity<Boolean> checkIfMemberOfTower(String towerId) {
    return ResponseEntity.ok(
        towerService.isUserMemberOfTower(
            authenticationFacade.getAuthentication().getName(), towerId));
  }

  @Override
  public ResponseEntity<TowerDto> createTower(TowerDto towerDto) {
    return ResponseEntity.ok(
        towerService.createTower(authenticationFacade.getAuthentication().getName(), towerDto));
  }

  @Override
  public ResponseEntity<TowerDto> getTowerById(String towerId) {
    return ResponseEntity.of(towerService.getTower(towerId));
  }

  @Override
  public ResponseEntity<Void> deleteTower(String towerId) {
    towerService.deleteTower(towerId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TowerDto> editTower(String towerId, TowerDto towerDto) {
    return ResponseEntity.of(towerService.editTower(towerId, towerDto));
  }

  @Override
  public ResponseEntity<TowerDto> joinTower(String towerId) {
    return ResponseEntity.ok(
        towerService.joinTower(authenticationFacade.getAuthentication().getName(), towerId));
  }

  @Override
  public ResponseEntity<TowerDto> leaveTower(String towerId) {
    return ResponseEntity.ok(
        towerService.leaveTower(authenticationFacade.getAuthentication().getName(), towerId));
  }

  @Override
  public ResponseEntity<List<TowerDto>> listTowers() {
    return ResponseEntity.ok(towerService.getTowers());
  }

  @Override
  public ResponseEntity<List<String>> listTowersIds() {
    // TODO: Implement
    throw new NotImplementedException(
        "The endpoint to list tower IDs isn't implemented yet. I might not even bother with it since we don't seem to have a use for it.");
  }
}
