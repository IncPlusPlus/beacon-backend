package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.CityInterserviceCommunicationsApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateTowerInviteRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UsernameAndPasswordDto;
import io.github.incplusplus.beacon.centralidentityserver.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CityInterserviceCommunicationsController implements CityInterserviceCommunicationsApi {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final CityService cityService;

  @Autowired
  public CityInterserviceCommunicationsController(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      CityService cityService) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
    this.cityService = cityService;
  }

  @Override
  public ResponseEntity<String> generateTowerInvite(
      CreateTowerInviteRequestDto createTowerInviteRequestDto) {
    // TODO Implement this
    return null;
  }

  @Override
  public ResponseEntity<NewCityDto> registerCity(String cityHostName) {
    NewCityDto registered = cityService.registerNewCity(cityHostName);
    return ResponseEntity.ok(registered);
  }

  @Override
  public ResponseEntity<Void> verifyUser(UsernameAndPasswordDto credentials) {
    UserDetails userDetails;
    try {
      userDetails = userDetailsService.loadUserByUsername(credentials.getUsername());
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
    if (passwordEncoder.matches(credentials.getPassword(), userDetails.getPassword())) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
