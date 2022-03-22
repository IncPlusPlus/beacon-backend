package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.TowersApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import io.github.incplusplus.beacon.city.service.TowerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class TowerController implements TowersApi {

  private final TowerService towerService;

  public TowerController(@Autowired TowerService towerService) {
    this.towerService = towerService;
  }

  @Override
  public ResponseEntity<TowerDto> createTower(TowerDto towerDto) {
    return ResponseEntity.ok(towerService.createTower(towerDto));
  }

  @Override
  public ResponseEntity<TowerDto> getTowerById(String towerId) {
    return ResponseEntity.of(towerService.getTower(towerId));
  }

  @Override
  public ResponseEntity<List<TowerDto>> listTowers() {
    return ResponseEntity.ok(towerService.getTowers());
  }

  @Override
  public ResponseEntity<List<String>> listTowersIds() {
    // TODO: Implement
    return null;
  }
}
