package io.github.incplusplus.beacon.city.controller;

import io.github.incplusplus.beacon.city.generated.controller.TowersApi;
import io.github.incplusplus.beacon.city.generated.dto.TowerDto;
import java.util.Collections;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class TowerController implements TowersApi {

  @Override
  public ResponseEntity<TowerDto> createTower(TowerDto towerDto) {
    return null;
  }

  @Override
  public ResponseEntity<TowerDto> getTowerById(String towerId) {
    return null;
  }

  @Override
  public ResponseEntity<List<TowerDto>> listTowers() {
    return ResponseEntity.ok(Collections.emptyList());
  }

  @Override
  public ResponseEntity<List<String>> listTowersIds() {
    return null;
  }
}
