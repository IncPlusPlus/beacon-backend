package io.github.incplusplus.beacon.centralidentityserver.controller;

import io.github.incplusplus.beacon.centralidentityserver.generated.controller.CityInterserviceCommunicationsApi;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.CreateTowerInviteRequestDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.NewCityDto;
import io.github.incplusplus.beacon.centralidentityserver.generated.dto.UserAccountDto;
import io.github.incplusplus.beacon.centralidentityserver.mapper.UserMapper;
import io.github.incplusplus.beacon.centralidentityserver.service.CityService;
import io.github.incplusplus.beacon.centralidentityserver.service.UserService;
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
  private final UserService userService;
  private final UserMapper userMapper;

  @Autowired
  public CityInterserviceCommunicationsController(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder,
      CityService cityService,
      UserService userService,
      UserMapper userMapper) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
    this.cityService = cityService;
    this.userService = userService;
    this.userMapper = userMapper;
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
  public ResponseEntity<UserAccountDto> verifyUser(String username, String password) {
    UserDetails userDetails;
    UserAccountDto userAccountDto;
    try {
      // TODO In the future, check these for null. Otherwise, a 500 happens.
      userDetails = userDetailsService.loadUserByUsername(username);
    } catch (UsernameNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
    if (passwordEncoder.matches(password, userDetails.getPassword())) {
      // We already performed a loadByUsername, so it should be pretty safe to get w/o isPresent()
      //noinspection OptionalGetWithoutIsPresent
      userAccountDto = userMapper.userToUserDto(userService.getAccountByUsername(username).get());
      return ResponseEntity.ok(userAccountDto);
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
