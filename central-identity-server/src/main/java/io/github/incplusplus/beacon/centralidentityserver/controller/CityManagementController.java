package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.CityManagementApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CityDto;
import io.github.incplusplus.beacon.centralidentityserver.service.CityService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CityManagementController implements CityManagementApi {
  private final CityService cityService;

  public CityManagementController(CityService cityService) {
    this.cityService = cityService;
  }

  @Override
  public ResponseEntity<List<CityDto>> listRegisteredCities() {
    return ResponseEntity.ok(cityService.getCities());
  }
}
