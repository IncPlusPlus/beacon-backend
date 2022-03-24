package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.CityManagementApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CityDto;
import io.github.incplusplus.beacon.centralidentityserver.security.IAuthenticationFacade;
import io.github.incplusplus.beacon.centralidentityserver.service.CityService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CityManagementController implements CityManagementApi {
  private final CityService cityService;
  private final IAuthenticationFacade authenticationFacade;

  public CityManagementController(
      CityService cityService, IAuthenticationFacade authenticationFacade) {
    this.cityService = cityService;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  public ResponseEntity<CityDto> getCity(String cityId) {
    return ResponseEntity.of(cityService.getCity(cityId));
  }

  @Override
  public ResponseEntity<List<CityDto>> listCitiesMemberOf() {
    return ResponseEntity.ok(
        cityService.listCitiesMemberOf(authenticationFacade.getAuthentication().getName()));
  }

  @Override
  public ResponseEntity<List<CityDto>> listRegisteredCities() {
    return ResponseEntity.ok(cityService.getCities());
  }
}
